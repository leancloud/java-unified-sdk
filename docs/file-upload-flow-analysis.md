# 文件上传与删除流程调研

本文基于以下代码和文档整理：

- Java SDK：当前仓库的 `core` 模块，版本配置见 `core/src/main/java/cn/leancloud/core/AppConfiguration.java`。
- 服务端：相邻仓库 `../uluru-platform`，主要实现位于 `uluru-api` 和 `uluru-commons`。
- 官方文档：[Java 文件](https://docs.leancloud.cn/sdk/storage/guide/java/#%E6%96%87%E4%BB%B6)。

## 结论

Java SDK 当前使用的 `/fileTokens` 上传流程不是“先上传 OSS，再写数据库”，而是：

```text
申请上传 token
    -> 创建 MongoDB _File 记录
    -> 返回 objectId/key/upload_url/token
    -> 客户端直传 OSS
    -> fileCallback(result=true)
    -> 清理 token 映射并触发后续审核
```

删除流程也不是一个数据库和 OSS 的原子操作，而是：

```text
DELETE _File
    -> MongoDB 同步删除
    -> 投递 Kafka file-event
    -> API consumer 异步删除 OSS 对象
```

因此，正常删除请求返回成功时，OSS 对象可能仍处于待清理状态。

服务端还保留旧版 `/files/:name`、`/qiniu` 和 `/s3` 接口。旧版二进制上传是“先写 OSS，再创建 `_File` 记录”，但 Java SDK 当前主流程走的是 `/fileTokens`，不走这条旧路径。

## 一、Java SDK 文件构建

### 1. 字节数组

`LCFile(String name, byte[] data)` 位于 `core/src/main/java/cn/leancloud/LCFile.java:77`：

- 计算 MD5；
- 将字节写入本地文件缓存；
- 写入 `_name`、`_checksum`、`size` metadata；
- 根据文件名推断 `mime_type`。

### 2. 本地文件

`LCFile(String name, File localFile)` 位于 `LCFile.java:97`：

- 校验文件存在且为普通文件；
- 计算 MD5；
- 保存原始绝对路径到 `localPath`；
- 写入文件大小、checksum 和 MIME 类型。

### 3. 外部 URL

`LCFile(String name, String url)` 位于 `LCFile.java:117`：

- 只保存 URL；
- metadata 写入 `__source=external`；
- 不下载原文件，也不上传 OSS。

保存外部 URL 时，SDK 直接创建 `_File` 对象，不经过 `/fileTokens` 和二进制传输。

## 二、Java SDK 当前上传流程

### 1. SDK 请求 `/fileTokens`

SDK 的上传入口是 `LCFile.saveWithProgressCallback()`，见 `LCFile.java:537`。未保存文件会调用：

```text
POST /1.1/fileTokens
```

Java API 定义在 `core/src/main/java/cn/leancloud/service/APIService.java:128`，调用封装在 `StorageClient.java:392`。

请求参数来自 `LCObject.generateChangedParam()`，包含文件名、metadata、MIME 类型、ACL 等操作数据。

### 2. 服务端选择 bucket、provider 和 key

服务端路由在：

```text
uluru-api/src/clj/uluru/api/handler.clj:490
```

处理函数为：

```text
uluru-api/src/clj/uluru/api/handlers/file.clj:279
```

服务端依次执行：

1. 校验 `_File` class 的写权限；
2. 获取或创建应用文件 bucket；
3. 使用客户端 key，或生成 UUID 文件 key；
4. 根据当前 provider 生成上传 URL；
5. 生成访问 URL；
6. 构造 `_File` 文档。

key 生成逻辑在 `file.clj:287-289`，`keep_file_name` 处理在 `file.clj:247-253`。

#### key 的具体规则

`/fileTokens` 中有两种 key 来源：

1. **未提供 `key`**：调用 `generate-api-key`，生成：

   ```text
   [允许的 prefix/] + uuid(32) + "/" + name
   ```

   `uuid(32)` 是从大小写字母和数字中随机生成的 32 个字符，不是 Java 标准 UUID 的 36 字符格式。若应用配置了文件 provider prefix，还会再加上 `prefix/`。默认生成路径因此具有约 `62^32`（约 190 bit）随机空间，正常情况下随机碰撞概率极低。

2. **提供 `key`**：调用 `get-client-key`。普通客户端的 key 只会经过 `validate-client-file-key` 的格式白名单校验（首段需匹配若干长度的字母数字/UUID 形式）；Master Key 可以绕过该校验。`keep_file_name=true` 时，服务端还会把文件名追加到客户端 key 后面。最后同样会根据 provider prefix 追加前缀。

当前代码没有看到为 `_File.key` 建立唯一索引，也没有在创建 `_File` 前查询或拒绝重复 key。因此 key 在数据库层面不是唯一约束。

### 3. 先创建 MongoDB `_File` 记录

服务端在 `file.clj:312-314` 调用：

```clojure
(file-delegator objects/add-object ...)
```

这一调用先创建 `_File` MongoDB 文档，并生成 `objectId`。只有当返回状态为 `201` 时，服务端才继续创建上传凭证。

因此，在客户端真正向 OSS 发送二进制数据之前，MongoDB 中已经存在一条 `_File` 记录。此时记录中的 `key`、`bucket`、`provider` 等信息已经确定，但 OSS 对象可能尚不存在。

### key 冲突与删除影响

对象存储通常以 `bucket + key` 作为对象地址，上传相同 key 的文件会覆盖或复用同一个 OSS 对象；MongoDB 中却可能存在多条 `_File` 记录。服务端删除文件时依据被删除记录中的 `key` 调用 provider 删除，并不根据 `objectId` 在 OSS 中区分对象。因此：

```text
_File A: key = K  ─┐
_File B: key = K  ─┴─> OSS bucket/K
删除 A -> 删除 OSS bucket/K -> B 的 URL 变成死链
```

这不是默认随机 key 的常规风险，而是以下情况可能触发的风险：

- 客户端显式复用同一个 key；
- Master Key 写入重复 key；
- 极低概率的随机 key 碰撞；
- 同一个 key 的并发上传或重试。

此外，如果旧上传的 `result=false` callback 晚于新上传到达，旧 token 对应的删除操作同样会按同一个 key 删除 OSS 对象，可能误伤新文件。`objectId` 只用于找到要删除的 `_File` 记录，不会为 OSS 对象提供隔离。

### 4. 生成上传 token 并返回客户端

成功创建 `_File` 后，服务端在 `file.clj:315-338`：

- 调用 `ufp/create-upload-token`；
- 生成 Qiniu、QCloud 或 S3 的 provider token；
- 返回 `objectId`、`key`、`bucket`、`provider`、`url`、`upload_url` 和 `token`；
- 将 `MD5(app_id + ":" + token) -> file objectId` 写入 Redis，过期时间 600 秒。

provider 实现位于：

- Qiniu：`uluru-commons/src/clj/uluru/file_provider.clj:281-315`；
- QCloud：`file_provider.clj:247-279`；
- S3：`file_provider.clj:326-363`。

### 5. SDK 直传对象存储

SDK 收到 token 后，将返回字段写入 `LCFile`，再根据 provider 创建上传器，见 `FileUploader.java:35-65`。

- Qiniu 使用 `mkblk`、`bput`、`mkfile` 分块上传；
- QCloud 小文件使用普通 multipart，大文件先建立 session，再以 512 KB 分片上传；
- S3 对预签名 `upload_url` 发 PUT 请求，SDK 会将文件整体读入 byte[]。

### 6. 上传完成 callback

SDK 上传结束后调用：

```text
POST /1.1/fileCallback
{
  "result": true,
  "token": "..."
}
```

客户端调用位置为 `LCFile.java:562-569`，服务端路由为 `handler.clj:491`，处理函数为 `file.clj:412`。

服务端 `result=true` 时：

1. 通过 `app_id + token` 查找 Redis 中缓存的 `_File objectId`；
2. 删除 Redis token 映射；
3. 如果启用了图片审核，则投递 `file-censor` Kafka 任务。

代码位于 `file.clj:415-427`。callback 不会再次创建 `_File`，因为数据库记录已经在 `/fileTokens` 阶段创建。

服务端 callback 也没有在这段代码中主动检查 OSS 对象是否存在，`result=true` 主要被视为客户端对上传成功的确认。

## 三、上传失败清理

如果 SDK 上传失败，会发送 `result=false` 的 callback。

服务端在 `file.clj:429-436`：

1. 通过 token 找到 `_File objectId`；
2. 在关闭权限和 hook 的上下文中删除 `_File`；
3. 复用文件删除流程，最终投递 OSS 删除事件。

因此失败路径是：

```text
OSS 上传失败
    -> fileCallback(result=false)
    -> 删除 MongoDB _File
    -> Kafka 异步删除 OSS key
```

如果客户端崩溃、网络断开或完全没有发送 callback，Redis token 会在 600 秒后过期，但代码中没有看到同一 token 超时后的自动 `_File` 清理逻辑，可能留下未完成的 `_File` 记录。

### 没有 callback 是否会形成死链

会，具体取决于 OSS 上传是否已经完成：

- **没有上传成功，也没有 callback**：`/fileTokens` 已经创建的 `_File` 记录不会因为 Redis token 在 600 秒后过期而自动删除；此时记录中的 `url/key` 指向尚不存在的 OSS 对象，查询或使用该文件时可能得到死链。
- **OSS 已上传成功，但没有 callback**：`_File` 记录和 OSS 对象都可能保留，通常不是死链，而是未完成回调、未触发图片审核的脏文件。是否能访问仍取决于 provider 和访问策略。
- **发送了 `result=false`，但 MongoDB 删除失败**：`_File` 记录会继续保留。如果此前 OSS 已写入失败或只写入了不完整对象，就会形成数据库中有记录、URL 不可访问的死链；如果 OSS 对象实际存在，则表现为未清理的有效文件或孤儿文件。

`result=false` 的删除调用位于 `file.clj:429-436`，复用 `delete-file`。对象删除流程是先执行 MongoDB `store/delete`，删除成功后才通过 `post-delete-object` 投递 Kafka；因此 MongoDB 删除失败时不会进入 OSS 删除事件。与此同时，`fileCallback` 没有检查内部删除响应，并在处理结束时无条件执行 `success`（`file.clj:437`），所以客户端可能收到成功响应，而 `_File` 实际仍然存在。若底层 MongoDB 删除抛出异常，最终 HTTP 行为还取决于统一异常处理，但不能据此认为记录一定会被清理。

## 四、文件删除流程

### 1. 客户端请求

Java SDK 继承 `LCObject.deleteInBackground()`，最终调用：

```text
DELETE /1.1/classes/_File/{objectId}
```

服务端文件路由为：

```text
uluru-api/src/clj/uluru/api/handler.clj:498
```

### 2. 同步删除 MongoDB

`objects/delete-one-object` 的顺序是：

1. 查询文件对象；
2. 校验删除权限和 ACL；
3. 执行 `store/delete` 删除 MongoDB 文档；
4. 执行 after-delete hook；
5. 调用 `after-delete-object`。

实现位于 `uluru-api/src/clj/uluru/api/handlers/objects.clj:397-422`，其中 MongoDB 删除在 `objects.clj:411`。

### 3. 投递 Kafka 删除事件

文件删除 endpoint 绑定了 `objects/post-delete-object`，删除成功后向 `file-event` topic 投递：

```clojure
{:app_id app_id
 :file file
 :type "delete-file"}
```

代码位于 `file.clj:357-362`，Kafka 封装位于 `uluru-commons/src/clj/uluru/kafka_queue.clj:35-36`。

### 4. Consumer 异步删除 OSS

API consumer 在 `uluru-api/src/clj/uluru/api/consumer.clj:154-158` 消费 `delete-file` 事件，调用 `file/post-delete-file`。

`post-delete-file` 位于 `file.clj:341-355`，会：

- 读取当前应用的 provider 配置；
- 检查文件 bucket 是否属于当前应用；
- 按文件 key 调用 provider 的 `delete-files`；
- 最多重试 3 次；
- 捕获异常后不再向上抛出。

provider 删除实现为：

- Qiniu：`uluru-commons/src/clj/uluru/qiniu.clj:122-129`；
- QCloud：`uluru-commons/src/clj/uluru/qcloud_cos.clj:190-207`；
- S3：`uluru-commons/src/clj/uluru/s3.clj:142-145`。

因此正常删除的真实时序是：

```text
DELETE 请求
    -> 同步删除 MongoDB _File
    -> 投递 Kafka
    -> HTTP 返回成功
    -> Consumer 异步调用 OSS 删除
```

### 5. 删除失败语义

MongoDB 删除和 OSS 删除不在同一个事务中：

- MongoDB 删除成功、Kafka 投递失败：可能留下 OSS 对象；
- Kafka 消费失败或 provider 删除失败：MongoDB 记录已经不存在，但 OSS 对象仍可能存在；
- OSS 删除失败被捕获后不会恢复 MongoDB 记录，也没有在这里看到持久化补偿记录。

因此，针对“回调 `false` 但记录删除失败”的问题，结论是：**存在死链风险，但不是每次都会是死链**。必要条件是 `_File` 仍存在且对应 OSS key 不存在或不可访问；若 OSS 对象存在，残留的是未清理记录/对象，而不是死链。

## 五、旧版直接上传接口

服务端仍保留以下旧接口：

- `POST /1.1/files/:name`；
- `POST /1.1/qiniu`；
- `POST /1.1/s3`。

路由见 `uluru-api/src/clj/uluru/api/handler.clj:503-508`。

旧版普通二进制上传在 `file.clj:165-211`，顺序是：

```text
读取请求数据
    -> 上传 provider
    -> provider 上传成功后创建 MongoDB _File
```

multipart 版本在 `file.clj:132-156`，同样是先 `save-file-and-get-url`，再执行 `objects/add-object`。

这与当前 `/fileTokens` 流程相反。分析 Java SDK 时应以 `/fileTokens` 为准，因为 Java SDK 的 `StorageClient.newUploadToken()` 明确调用该接口。

## 六、客户端与服务端联动问题

### 1. Java SDK 重复发送 fileCallback

Java SDK 中 `FileUploader.execute()` 已经发送一次 callback，见 `FileUploader.java:42-49`；随后 `LCFile.java:564-569` 又发送一次。

服务端第一次成功 callback 会删除 Redis token 映射，第二次 callback 找不到对应 file id，并在 `file.clj:428` 记录错误日志。它不会重新创建文件，但会造成重复请求和告警。

### 2. 删除是最终一致而不是原子删除

MongoDB 删除成功即可向客户端返回成功，OSS 删除通过 Kafka 异步执行。当前实现需要依赖 Kafka 和 consumer 正常运行，且 provider 删除失败不会回滚数据库。

### 3. provider 切换可能影响历史文件删除

`post-delete-file` 使用当前应用的 provider 配置，再根据 bucket 做归属校验。如果应用在文件创建后切换 provider，历史文件的 provider 信息虽然保存在 `_File` 文档中，但删除流程使用的是当前配置，需确认生产环境是否允许 provider 切换，以及是否有迁移兼容逻辑。

### 4. callback 主要依赖客户端结果

`fileCallback(result=true)` 的实现主要校验 token 映射并清理缓存，没有看到对 OSS 对象做 HEAD 或元数据校验。因此服务端是否能够保证“数据库记录对应的 OSS 对象确实存在”，取决于客户端上传器和 provider 本身，而不是 callback 阶段的二次校验。

## 总结

对 Java SDK 当前主链路，可以准确概括为：

```text
创建：MongoDB _File -> OSS 上传 -> callback 确认
失败：删除 MongoDB _File -> Kafka 异步删除 OSS
删除：删除 MongoDB _File -> Kafka 异步删除 OSS
```

只有服务端保留的旧版 `/files/:name`、`/qiniu`、`/s3` 接口才是“先 OSS、后 MongoDB”。
