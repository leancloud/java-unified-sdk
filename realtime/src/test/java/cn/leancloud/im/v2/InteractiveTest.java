package cn.leancloud.im.v2;

import cn.leancloud.AVLogger;
import cn.leancloud.Configure;
import cn.leancloud.core.AVOSCloud;
import cn.leancloud.im.AVIMOptions;
import cn.leancloud.im.v2.callback.*;
import cn.leancloud.session.AVConnectionManager;
import cn.leancloud.utils.StringUtil;
import cn.leancloud.json.JSONObject;
import junit.framework.TestCase;

import java.util.*;
import java.util.concurrent.CountDownLatch;

public class InteractiveTest extends TestCase {
  private String targetConversationId = null;
  CountDownLatch firstStage = null;
  CountDownLatch secondStage = null;
  CountDownLatch endStage = null;
  boolean testSucceed = false;

  public InteractiveTest(String name) {
    super(name);
    AVOSCloud.setLogLevel(AVLogger.Level.DEBUG);
    Configure.initialize();
    AVIMOptions.getGlobalOptions().setTimeoutInSecs(30);
//    AVIMOptions.getGlobalOptions().setRtmServer("wss://rtm51.leancloud.cn");
    AVIMMessageManager.setConversationEventHandler(new AVIMConversationEventHandler() {
      @Override
      public void onMemberLeft(AVIMClient client, AVIMConversation conversation, List<String> members, String kickedBy) {

      }

      @Override
      public void onMemberJoined(AVIMClient client, AVIMConversation conversation, List<String> members, String invitedBy) {

      }

      @Override
      public void onKicked(AVIMClient client, AVIMConversation conversation, String kickedBy) {

      }

      @Override
      public void onInvited(AVIMClient client, AVIMConversation conversation, String operator) {

      }

      @Override
      public void onInfoChanged(AVIMClient client, AVIMConversation conversation, JSONObject attr,
                                String operator) {
        super.onInfoChanged(client, conversation, attr, operator);
      }
    });

    AVIMMessageManager.registerDefaultMessageHandler(new AVIMMessageHandler());
  }

  @Override
  protected void setUp() throws Exception {
    AVConnectionManager manager = AVConnectionManager.getInstance();
    manager.autoConnection();
    Thread.sleep(2000);
    firstStage = new CountDownLatch(1);
    secondStage = new CountDownLatch(1);
    endStage = new CountDownLatch(1);
    testSucceed = false;
  }

  private boolean verifyConversationWithExpect(AVIMConversation conversation, Map<String, Object> expectedResult) {
    if (null == conversation || null == expectedResult || expectedResult.size() < 1) {
      return true;
    }
    boolean result = true;
    for (Map.Entry<String, Object> entry: expectedResult.entrySet()) {
      String key = entry.getKey();
      Object expect = entry.getValue();
      if ("name".equalsIgnoreCase(key)) {
        String convName = conversation.getName();
        if (!convName.equals(expect)) {
          System.out.println("❌Conversation name not match, expected=" + expect + ", actual=" + convName);
          result = false;
        } else {
          System.out.println("Conversation name matches!");
        }
      } else if ("memberSize".equalsIgnoreCase(key)) {
        int actual = conversation.getMembers().size();
        if (actual != (int)expect) {
          System.out.println("❌Conversation member size not match, expected=" + expect + ", actual=" + actual);
          result = false;
        } else {
          System.out.println("Conversation memberSize matches!");
        }
      } else if (key.startsWith("attr.")) {
        String attr = key.substring("attr.".length());
        Object actual = conversation.getAttribute(attr);
        if (actual == expect) {
          // for all NULL
          System.out.println("Conversation attributes matches!");
        } else if (null == actual || !actual.equals(expect)) {
          System.out.println("❌Conversation attribute not match, key=" + attr + ", expected=" + expect + ", actual=" + actual);
          result = false;
        } else {
          System.out.println("Conversation attributes matches!");
        }
      }
    }
    return result;
  }

  public void testAttributesWithSingleClient() throws Exception {
    final String thirdMember = StringUtil.getRandomString(8);
    final String clientId = "TestUserA";
    final String customAttr = StringUtil.getRandomString(16);
    final Date now = new Date();
    AVIMClient currentClient = AVIMClient.getInstance(clientId);
    final CountDownLatch latch = new CountDownLatch(1);
    currentClient.open(new AVIMClientCallback() {
      @Override
      public void done(AVIMClient client, AVIMException e) {
        if (null != e) {
          System.out.println("failed to open client:" + clientId);
          e.printStackTrace();
          latch.countDown();
          return;
        }
        System.out.println("☑️ " + clientId + " try to create conversation...");
        Map<String, Object> attr = new HashMap<>();
        attr.put("attr1", customAttr);
        attr.put("attr2", now);
        List<String> members = new ArrayList<>();
        members.add(thirdMember);

        client.createConversation(members, "testAttributesWithSingleClient", attr, new AVIMConversationCreatedCallback() {
          @Override
          public void done(final AVIMConversation conversation, AVIMException e) {
            if (null != e) {
              System.out.println(clientId + " failed to create conversation: testAttributesWithSingleClient");
              e.printStackTrace();
              latch.countDown();
              return;
            }
            System.out.println("☑️☑️ " + clientId + " succeed to create conversation, data=" + conversation.toJSONString());
            Map<String, Object> checkpoint = new HashMap<>();
            checkpoint.put("name", "testAttributesWithSingleClient");
            checkpoint.put("memberSize", 2);
            checkpoint.put("attr.attr1", customAttr);
            checkpoint.put("attr.attr2", now);
            boolean assertResult = verifyConversationWithExpect(conversation, checkpoint);
            if (!assertResult) {
              System.out.println("❌　Site:" + clientId + " conversation doesn't match expected.");
            } else {
              System.out.println("checkpoint all passed.");
            }

            System.out.println("☑️☑️ " + clientId + " continue to modify conversation...");

            conversation.setName("MemberListIsVerified");
            conversation.setAttribute("attr1", "Over");
            conversation.set("attr2", null);
            conversation.updateInfoInBackground(new AVIMConversationCallback() {
              @Override
              public void done(AVIMException e) {
                if (null != e) {
                  System.out.println("failed to update conversationinfo.");
                  e.printStackTrace();
                  latch.countDown();
                  return;
                }
                System.out.println("☑️☑️☑️ " + clientId + " already modified conversation. data=" + conversation.toJSONString());
                Map<String, Object> checkpoint = new HashMap<>();
                checkpoint.put("name", "MemberListIsVerified");
                checkpoint.put("memberSize", 2);
                checkpoint.put("attr.attr1", "Over");
                boolean assertResult = verifyConversationWithExpect(conversation, checkpoint);
                if (!assertResult) {
                  System.out.println("❌　Site:" + clientId + " conversation doesn't match expected.");
                } else {
                  System.out.println("checkpoint all passed.");
                }
                conversation.kickMembers(Arrays.asList(thirdMember), new AVIMOperationPartiallySucceededCallback() {
                  @Override
                  public void done(AVIMException e, List<String> successfulClientIds, List<AVIMOperationFailure> failures) {
                    if (null != e) {
                      System.out.println("failed to kick member.");
                      e.printStackTrace();
                      latch.countDown();
                      return;
                    } else {
                      System.out.println("☑️☑️☑️☑️ " + clientId + " kick members finished. data=" + conversation.toJSONString());
                    }
                    if (null != successfulClientIds && successfulClientIds.size() == 1 && thirdMember.equals(successfulClientIds.get(0))) {
                      System.out.println("☑️☑️☑️☑️ successful client list is right.");
                    } else {
                      System.out.println("❌　successful client list is wrong.");
                      System.out.println("failure list is: " + failures);
                    }

                    Map<String, Object> checkpoint = new HashMap<>();
                    checkpoint.put("name", "MemberListIsVerified");
                    checkpoint.put("memberSize", 1);
                    checkpoint.put("attr.attr1", "Over");
                    boolean assertResult = verifyConversationWithExpect(conversation, checkpoint);
                    if (!assertResult) {
                      System.out.println("❌　Site:" + clientId + " conversation doesn't match expected.");
                    } else {
                      testSucceed = true;
                      System.out.println("checkpoint all passed.");
                    }
                    latch.countDown();
                  }
                });
              }
            });
          }
        });
      }
    });
    latch.await();
    assertTrue(testSucceed);
  }

  public void testCorrectMemberList() throws Exception {
    final String thirdMember = StringUtil.getRandomString(8);
    final String customAttr = StringUtil.getRandomString(8);

    System.out.println("Main Thread: " + Thread.currentThread().getId());
    Thread firstThread = new Thread(new Runnable() {
      @Override
      public void run() {
        System.out.println("First Thread: " + Thread.currentThread().getId());
        final CountDownLatch exitLatch = new CountDownLatch(1);
        final String clientId = "TestUserA";
        final AVIMClient currentClient = AVIMClient.getInstance(clientId);

        currentClient.open(new AVIMClientCallback() {
          @Override
          public void done(AVIMClient client, AVIMException e) {
            if (null != e) {
              System.out.println("failed to open client:" + clientId);
              e.printStackTrace();
              exitLatch.countDown();
              return;
            }
            System.out.println("☑️ " + clientId + " try to create conversation...");
            Map<String, Object> attr = new HashMap<>();
            attr.put("attr1", customAttr);
            List<String> members = new ArrayList<>();
            members.add(thirdMember);
            client.createConversation(members, "testCorrectMemberList", attr, new AVIMConversationCreatedCallback() {
              @Override
              public void done(final AVIMConversation conversation, AVIMException e) {
                if (null != e) {
                  System.out.println(clientId + " failed to create conversation: testCorrectMemberList");
                  e.printStackTrace();
                  exitLatch.countDown();
                  return;
                }
                targetConversationId = conversation.getConversationId();

                System.out.println("☑️ " + clientId + " succeed to create conversation, data=" + conversation.toJSONString());
                Map<String, Object> checkpoint = new HashMap<>();
                checkpoint.put("name", "testCorrectMemberList");
                checkpoint.put("memberSize", 2);
                checkpoint.put("attr.attr1", customAttr);
                boolean assertResult = verifyConversationWithExpect(conversation, checkpoint);
                if (!assertResult) {
                  System.out.println("❌　Site:" + clientId + " conversation doesn't match expected.");
                } else {
                  System.out.println("checkpoint all passed.");
                }

                System.out.println("☑️ Thread with " + clientId + " try to notify other thread.");
                firstStage.countDown();
              }
            });
          }
        });
        try {
          System.out.println(clientId + " try to wait second thread running...");
          secondStage.await();

          System.out.println("☑️☑️ " + clientId + " continue to modify conversation...");
          final AVIMConversation conversation = currentClient.getConversation(targetConversationId);
          conversation.setName("MemberListIsVerified");
          conversation.setAttribute("attr1", "Over");
          conversation.updateInfoInBackground(new AVIMConversationCallback() {
            @Override
            public void done(AVIMException e) {
              if (null != e) {
                System.out.println("failed to update conversationinfo.");
                e.printStackTrace();
                exitLatch.countDown();
                return;
              }
              System.out.println("☑️☑️☑️ " + clientId + " already modified conversation. data=" + conversation.toJSONString());
              Map<String, Object> checkpoint = new HashMap<>();
              checkpoint.put("name", "MemberListIsVerified");
              checkpoint.put("memberSize", 3);
              checkpoint.put("attr.attr1", "Over");
              boolean assertResult = verifyConversationWithExpect(conversation, checkpoint);
              if (!assertResult) {
                System.out.println("❌　Site:" + clientId + " conversation doesn't match expected.");
              } else {
                System.out.println("checkpoint all passed.");
              }
              try {
                System.out.println("☑️☑️☑️☑️ " + clientId + " prepare to exit thread.");
                endStage.countDown();
              } catch (Exception ex) {
                ex.printStackTrace();
              }
              currentClient.close(new AVIMClientCallback() {
                @Override
                public void done(AVIMClient client, AVIMException e) {
                  ;
                }
              });
              exitLatch.countDown();
            }
          });
          exitLatch.await();
        } catch (Exception ex) {
          ex.printStackTrace();
        }
        System.out.println("First Thread exit");
      }
    });

    Thread secondThread = new Thread(new Runnable() {
      @Override
      public void run() {
        System.out.println("Second Thread: " + Thread.currentThread().getId() + " wait first thread running...");
        try {
          firstStage.await();
        } catch (Exception ex) {
          ex.printStackTrace();
          return;
        }
        final String clientId = "TestUserB";
        AVIMClient currentClient = AVIMClient.getInstance(clientId);
        System.out.println("☑️️ " + clientId + " try to openClient");
        currentClient.open(new AVIMClientCallback() {
          @Override
          public void done(AVIMClient client, AVIMException e) {
            if (null != e) {
              System.out.println("failed to open client:" + clientId);
              e.printStackTrace();
              return;
            }
            System.out.println("☑️️ " + clientId + " try to fetch target conversation:" + targetConversationId);
            final AVIMConversation conversation = client.getConversation(targetConversationId);
            conversation.fetchInfoInBackground(new AVIMConversationCallback() {
              @Override
              public void done(AVIMException e) {
                if (null != e) {
                  System.out.println("failed to fetch conversation:" + targetConversationId + " with clientId:" + clientId);
                  e.printStackTrace();
                  secondStage.countDown();
                  return;
                }
                System.out.println("☑️️☑️️ " + clientId + " try to join target conversation. data=" + conversation.toJSONString());
                conversation.join(new AVIMConversationCallback() {
                  @Override
                  public void done(AVIMException e) {
                    if (null != e) {
                      System.out.println("failed to join conversation:" + targetConversationId + " with clientId:" + clientId);
                      e.printStackTrace();
                      secondStage.countDown();
                      return;
                    }
                    System.out.println("☑️️☑️️☑️️ " + clientId + " joined target conversation. data=" + conversation.toJSONString());
                    Map<String, Object> checkpoint = new HashMap<>();
                    checkpoint.put("name", "testCorrectMemberList");
                    checkpoint.put("memberSize", 3);
                    checkpoint.put("attr.attr1", customAttr);
                    boolean assertResult = verifyConversationWithExpect(conversation, checkpoint);
                    if (!assertResult) {
                      System.out.println("❌　Site:" + clientId + " conversation doesn't match expected. conv=" + conversation.toJSONString());
                    } else {
                      System.out.println("checkpoint all passed.");
                    }

                    System.out.println("☑️️☑️️☑️️ " + clientId + " try to kick member:" + thirdMember + " from clientId:" + clientId);
                    conversation.kickMembers(Arrays.asList(thirdMember), new AVIMOperationPartiallySucceededCallback() {
                      @Override
                      public void done(AVIMException e, List<String> successfulClientIds, List<AVIMOperationFailure> failures) {
                        if (null == e && null != successfulClientIds && successfulClientIds.size() > 0) {
                          Map<String, Object> checkpoint = new HashMap<>();
                          checkpoint.put("name", "testCorrectMemberList");
                          checkpoint.put("memberSize", 2);
                          checkpoint.put("attr.attr1", customAttr);
                          boolean assertResult = verifyConversationWithExpect(conversation, checkpoint);
                          if (!assertResult) {
                            System.out.println("Site:" + clientId + " conversation doesn't match expected. conv=" + conversation.toJSONString());
                          }
                        } else {
                          Map<String, Object> checkpoint = new HashMap<>();
                          checkpoint.put("name", "testCorrectMemberList");
                          checkpoint.put("memberSize", 3);
                          checkpoint.put("attr.attr1", customAttr);
                          boolean assertResult = verifyConversationWithExpect(conversation, checkpoint);
                          if (!assertResult) {
                            System.out.println("Site:" + clientId + " conversation doesn't match expected. conv=" + conversation.toJSONString());
                          }
                          if (null != e) {
                            System.out.println("failed to kick member:" + thirdMember + ", exception:" + e.getMessage());
                          } else if (null != failures && failures.size() > 0){
                            AVIMOperationFailure failure = failures.get(0);
                            System.out.println("failed to kick member:" + failure.getMemberIds()
                                    + ", reason:" + failure.getReason());
                          } else {
                            System.out.println("failure list is:" + failures);
                          }
                        }

                        secondStage.countDown();
                        System.out.println("☑️️☑️️☑️️ " + clientId + " try to wait first thread running...");
                      }
                    });
                  }
                });
              }
            });
          }
        });
        try {
          endStage.await();

          System.out.println("☑️️☑️️☑️️☑️️ " + clientId + " got notification to exit thread.");
          Thread.sleep(3000);

          AVIMConversation conversation = currentClient.getConversation(targetConversationId);

          Map<String, Object> checkpoint = new HashMap<>();
          checkpoint.put("name", "MemberListIsVerified");
          checkpoint.put("memberSize", 3);
          checkpoint.put("attr.attr1", "Over");
          boolean assertResult = verifyConversationWithExpect(conversation, checkpoint);
          if (!assertResult) {
            System.out.println("❌　Site:" + clientId + " conversation doesn't match expected. conv=" + conversation.toJSONString());
          } else {
            System.out.println("checkpoint all passed.");
          }
          System.out.println("☑️️☑️️☑️️☑️️ " + clientId + " prepare to exit thread.");
          currentClient.close(new AVIMClientCallback() {
            @Override
            public void done(AVIMClient client, AVIMException e) {
              ;
            }
          });
        } catch (Exception ex) {
          ex.printStackTrace();
        }
        System.out.println("Second Thread exit");
      }
    });
    firstThread.start();
    secondThread.start();
    firstThread.join();
    secondThread.join();
    System.out.println("Main Thread exit");
  }

  public void testCorrectConversationData() throws Exception {
    final String thirdMember = StringUtil.getRandomString(8);
    final String customAttr = StringUtil.getRandomString(8);
    final Date now = new Date();

    System.out.println("Main Thread: " + Thread.currentThread().getId());
    Thread firstThread = new Thread(new Runnable() {
      @Override
      public void run() {
        System.out.println("First Thread: " + Thread.currentThread().getId());
        final CountDownLatch exitLatch = new CountDownLatch(1);

        final String clientId = "TestUserA";
        AVIMClient currentClient = AVIMClient.getInstance(clientId);

        currentClient.open(new AVIMClientCallback() {
          @Override
          public void done(AVIMClient client, AVIMException e) {
            if (null != e) {
              System.out.println("failed to open client:" + clientId);
              e.printStackTrace();
              exitLatch.countDown();
              return;
            }
            System.out.println("☑️ " + clientId + " try to create conversation...");
            Map<String, Object> attr = new HashMap<>();
            attr.put("attr1", customAttr);
            List<String> members = new ArrayList<>();
            members.add(thirdMember);
            client.createConversation(members, "testCorrectMemberList", attr, new AVIMConversationCreatedCallback() {
              @Override
              public void done(final AVIMConversation conversation, AVIMException e) {
                if (null != e) {
                  System.out.println(clientId + " failed to create conversation: testCorrectMemberList");
                  e.printStackTrace();
                  exitLatch.countDown();
                  return;
                }
                System.out.println(conversation.get("attr.attr1"));
                System.out.println(conversation.getAttribute("attr1"));
                targetConversationId = conversation.getConversationId();

                System.out.println("☑️ " + clientId + " succeed to create conversation, id=" + targetConversationId);
                Map<String, Object> checkpoint = new HashMap<>();
                checkpoint.put("name", "testCorrectMemberList");
                checkpoint.put("memberSize", 2);
                checkpoint.put("attr.attr1", customAttr);
                boolean assertResult = verifyConversationWithExpect(conversation, checkpoint);
                if (!assertResult) {
                  System.out.println("❌　Site:" + clientId + " conversation doesn't match expected. conv=" + conversation.toJSONString());
                } else {
                  System.out.println("checkpoint all passed.");
                }

                System.out.println("☑️ Thread with " + clientId + " try to notify other thread.");
                firstStage.countDown();
              }
            });
          }
        });
        try {
          System.out.println("☑️☑️ " + clientId + " try to wait second thread running...");
          secondStage.await();

          System.out.println("☑️☑️ " + clientId + " continue to modify conversation...");
          final AVIMConversation conversation = currentClient.getConversation(targetConversationId);
          //conversation.setName("MemberListIsVerified");
          conversation.set("attr.attr2", now);
          final Map<String, String> deleteOperation = new HashMap<>();
          deleteOperation.put("__op", "Delete");
          conversation.set("attr.attr1", deleteOperation);
          final Map<String, String> deleteOp = new HashMap<>();
          deleteOp.put("__op", "Delete");
          conversation.set("name", deleteOp);
          conversation.setAttribute("attr3", "nothing");
          conversation.updateInfoInBackground(new AVIMConversationCallback() {
            @Override
            public void done(AVIMException e) {
              if (null != e) {
                System.out.println("failed to update conversationinfo.");
                e.printStackTrace();
                exitLatch.countDown();
                return;
              }
              System.out.println("☑️☑️☑️ " + clientId + " already modified conversation...");
              Map<String, Object> checkpoint = new HashMap<>();
              checkpoint.put("name", "MemberListIsVerified");
              checkpoint.put("memberSize", 3);
              checkpoint.put("attr.attr1", deleteOp);
              checkpoint.put("attr.attr2", now);
              boolean assertResult = verifyConversationWithExpect(conversation, checkpoint);
              if (!assertResult) {
                System.out.println("❌　Site:" + clientId + " conversation doesn't match expected. conv=" + conversation.toJSONString());
              } else {
                System.out.println("checkpoint all passed.");
              }
              try {
                System.out.println("☑️☑️☑️☑️ " + clientId + " prepare to exit thread.");
                endStage.countDown();
              } catch (Exception ex) {
                ex.printStackTrace();
              }
              exitLatch.countDown();
            }
          });
          exitLatch.await();
          currentClient.close(new AVIMClientCallback() {
            @Override
            public void done(AVIMClient client, AVIMException e) {
              ;
            }
          });
        } catch (Exception ex) {
          ex.printStackTrace();
        }
        System.out.println("First Thread exit");
      }
    });

    Thread secondThread = new Thread(new Runnable() {
      @Override
      public void run() {
        System.out.println("Second Thread: " + Thread.currentThread().getId());
        try {
          System.out.println("wait first thread running...");
          firstStage.await();
        } catch (Exception ex) {
          ex.printStackTrace();
          return;
        }
        final String clientId = "TestUserB";
        AVIMClient currentClient = AVIMClient.getInstance(clientId);
        System.out.println("☑️️ " + clientId + " try to openClient");
        currentClient.open(new AVIMClientCallback() {
          @Override
          public void done(AVIMClient client, AVIMException e) {
            if (null != e) {
              System.out.println("failed to open client:" + clientId);
              e.printStackTrace();
              return;
            }
            System.out.println("☑️️ " + clientId + " try to fetch target conversation:" + targetConversationId);
            final AVIMConversation conversation = client.getConversation(targetConversationId);
            conversation.fetchInfoInBackground(new AVIMConversationCallback() {
              @Override
              public void done(AVIMException e) {
                if (null != e) {
                  System.out.println("failed to fetch conversation:" + targetConversationId + " with clientId:" + clientId);
                  e.printStackTrace();
                  secondStage.countDown();
                  return;
                }
                System.out.println("☑️️☑️️ " + clientId + " try to join target conversation:" + targetConversationId);
                conversation.join(new AVIMConversationCallback() {
                  @Override
                  public void done(AVIMException e) {
                    if (null != e) {
                      System.out.println("failed to join conversation:" + targetConversationId + " with clientId:" + clientId);
                      e.printStackTrace();
                    } else {
                      Map<String, Object> checkpoint = new HashMap<>();
                      checkpoint.put("name", "testCorrectMemberList");
                      checkpoint.put("memberSize", 3);
                      checkpoint.put("attr.attr1", customAttr);
                      boolean assertResult = verifyConversationWithExpect(conversation, checkpoint);
                      if (!assertResult) {
                        System.out.println("❌　Site:" + clientId + " conversation doesn't match expected. conv=" + conversation.toJSONString());
                      } else {
                        System.out.println("checkpoint all passed.");
                      }

                      System.out.println("☑️️☑️️☑️️ " + clientId + " has joined.");
                    }
                    secondStage.countDown();
                  }
                });
              }
            });
          }
        });
        try {
          endStage.await();

          System.out.println("☑️️☑️️☑️️☑️️ " + clientId + " got notification to exit thread.");
          Thread.sleep(30000);

          AVIMConversation conversation = currentClient.getConversation(targetConversationId);

          Map<String, Object> checkpoint = new HashMap<>();
          checkpoint.put("name", "MemberListIsVerified");
          checkpoint.put("memberSize", 3);
          checkpoint.put("attr.attr1", null);
          checkpoint.put("attr.attr2", now.getTime());
          boolean assertResult = verifyConversationWithExpect(conversation, checkpoint);
          if (!assertResult) {
            System.out.println("❌　Site:" + clientId + " conversation doesn't match expected. conv=" + conversation.toJSONString());
          } else {
            System.out.println("checkpoint all passed.");
          }
          System.out.println("☑️️☑️️☑️️☑️️ " + clientId + " prepare to exit thread.");
        } catch (Exception ex) {
          ex.printStackTrace();
        }
        currentClient.close(new AVIMClientCallback() {
          @Override
          public void done(AVIMClient client, AVIMException e) {
            ;
          }
        });
        System.out.println("Second Thread exit");
      }
    });
    firstThread.start();
    secondThread.start();
    firstThread.join();
    secondThread.join();
    System.out.println("Main Thread exit");
  }

  public void testSendBinaryMessage() throws Exception {

  }
  public void testCorrectLastMessageNotification() throws Exception {
    ;
  }
}
