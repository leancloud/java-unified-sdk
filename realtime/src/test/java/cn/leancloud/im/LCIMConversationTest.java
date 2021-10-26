package cn.leancloud.im;

import cn.leancloud.LCException;
import cn.leancloud.LCQuery;
import cn.leancloud.Configure;
import cn.leancloud.im.v2.*;
import cn.leancloud.im.v2.callback.*;
import cn.leancloud.im.v2.conversation.LCIMConversationMemberInfo;
import cn.leancloud.im.v2.conversation.ConversationMemberRole;
import cn.leancloud.im.v2.messages.LCIMRecalledMessage;
import cn.leancloud.im.v2.messages.LCIMTextMessage;
import cn.leancloud.session.LCConnectionManager;
import cn.leancloud.utils.StringUtil;
import cn.leancloud.json.JSON;
import junit.framework.TestCase;

import java.util.*;
import java.util.concurrent.CountDownLatch;

public class LCIMConversationTest extends TestCase {
  private CountDownLatch countDownLatch = null;
  private boolean opersationSucceed = false;
  private LCIMClient client = null;
  private List<String> memebers = Arrays.asList("User2", "User3");
  private String convName = "RealtimeUnitTest";
  private DummyConversationEventHandler conversationEventHandler =
          new DummyConversationEventHandler(0x00FFFF);
  String testConversationId = null;

  public LCIMConversationTest(String suiteName) {
    super(suiteName);
    Configure.initialize();
    LCIMClient.setClientEventHandler(new DummyClientEventHandler());
    LCIMMessageManager.setConversationEventHandler(conversationEventHandler);
    LCConnectionManager manager = LCConnectionManager.getInstance();
    manager.autoConnection();
    try {
      Thread.sleep(1000);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  @Override
  protected void setUp() throws Exception {
    this.countDownLatch = new CountDownLatch(1);
    opersationSucceed = false;
    conversationEventHandler.resetAllCount();
    testConversationId = null;
  }

  @Override
  protected void tearDown() throws Exception {
    this.countDownLatch = null;
    final CountDownLatch tmpCounter = new CountDownLatch(1);
    if (null != client) {
      client.close(new LCIMClientCallback() {
        @Override
        public void done(LCIMClient client, LCIMException e) {
          tmpCounter.countDown();
        }
      });
      tmpCounter.await();
    }
    client = null;
  }

  public void testSendTextMessage() throws Exception {
    final CountDownLatch tmpCounter = new CountDownLatch(1);
    client = LCIMClient.getInstance("testUser1");
    client.open(new LCIMClientCallback() {
      @Override
      public void done(LCIMClient client, LCIMException e) {
        tmpCounter.countDown();
      }
    });
    tmpCounter.await();
    client.createConversation(memebers, convName, null, false, true, new LCIMConversationCreatedCallback() {
      @Override
      public void done(LCIMConversation conversation, LCIMException e) {
        if (null != e) {
          e.printStackTrace();
          countDownLatch.countDown();
        } else {
          LCIMTextMessage msg = new LCIMTextMessage();
          msg.setText("test run @" + System.currentTimeMillis());
          conversation.sendMessage(msg, new LCIMConversationCallback() {
            @Override
            public void done(LCIMException ex) {
              if (null != ex) {
                System.out.println("failed to send message");
                ex.printStackTrace();
              } else {
                System.out.println("succeed to send message");
                opersationSucceed = true;
              }
              countDownLatch.countDown();
            }
          });
        }
      }
    });
    countDownLatch.await();
    assertTrue(opersationSucceed);
  }

  public void testSendAndReceiveBinaryMessage() throws Exception {
    LCIMMessageManager.registerDefaultMessageHandler(new DummyMessageHandler());
    final CountDownLatch tmpCounter = new CountDownLatch(1);
    final CountDownLatch tmpCounter2 = new CountDownLatch(1);

    Thread firstThread = new Thread(new Runnable() {
      private LCIMConversation targetConversation = null;
      @Override
      public void run() {
        client = LCIMClient.getInstance("testUser1");
        client.open(new LCIMClientCallback() {
          @Override
          public void done(LCIMClient client, LCIMException e) {
            client.createConversation(memebers, convName, null, false, true, new LCIMConversationCreatedCallback() {
              @Override
              public void done(LCIMConversation conversation, LCIMException e) {
                if (null != e) {
                  e.printStackTrace();
                  tmpCounter.countDown();
                  countDownLatch.countDown();
                } else {
                  targetConversation = conversation;
                  tmpCounter.countDown();
                }
              }
            });
          }
        });
        try {
          tmpCounter2.await();
        } catch (Exception ex) {
          ex.printStackTrace();
        }
        LCIMBinaryMessage msg = new LCIMBinaryMessage();
        msg.setBytes(StringUtil.getRandomString(16).getBytes());
        targetConversation.sendMessage(msg, new LCIMConversationCallback() {
          @Override
          public void done(LCIMException ex) {
            if (null != ex) {
              System.out.println("failed to send binary message");
              ex.printStackTrace();
            } else {
              System.out.println("succeed to send binary message");
              opersationSucceed = true;
            }
            countDownLatch.countDown();
          }
        });
      }
    });
    Thread secondThread = new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          tmpCounter.await();
        } catch (Exception ex) {
          ex.printStackTrace();
        }
        LCIMClient client2 = LCIMClient.getInstance("User2");
        client2.open(new LCIMClientCallback() {
          @Override
          public void done(LCIMClient client, LCIMException e) {
            if (null != e) {
              System.out.println("User2 open failed.");
            }
            tmpCounter2.countDown();
          }
        });
      }
    });

    firstThread.start();
    secondThread.start();
    countDownLatch.await();

    firstThread.join();
    secondThread.join();
    assertTrue(opersationSucceed);
  }

  public void testSendAndReceiveTextMessage() throws Exception {
    LCIMMessageManager.registerDefaultMessageHandler(new DummyMessageHandler());
    final CountDownLatch tmpCounter = new CountDownLatch(1);
    final CountDownLatch tmpCounter2 = new CountDownLatch(1);
    client = LCIMClient.getInstance("testUser1");
    client.open(new LCIMClientCallback() {
      @Override
      public void done(LCIMClient client, LCIMException e) {
        tmpCounter.countDown();
      }
    });
    tmpCounter.await();
    client.createConversation(memebers, convName, null, false, true, new LCIMConversationCreatedCallback() {
      @Override
      public void done(LCIMConversation conversation, LCIMException e) {
        if (null != e) {
          e.printStackTrace();
          countDownLatch.countDown();
        } else {
          try {
            tmpCounter2.await();
          } catch (Exception ex) {
            ex.printStackTrace();
          }
          LCIMTextMessage msg = new LCIMTextMessage();
          msg.setText("test run @" + System.currentTimeMillis());
          conversation.sendMessage(msg, new LCIMConversationCallback() {
            @Override
            public void done(LCIMException ex) {
              if (null != ex) {
                System.out.println("failed to send message");
                ex.printStackTrace();
              } else {
                System.out.println("succeed to send message");
                opersationSucceed = true;
              }
              countDownLatch.countDown();
            }
          });
        }
      }
    });
    LCIMClient client2 = LCIMClient.getInstance("User2");
    client2.open(new LCIMClientCallback() {
      @Override
      public void done(LCIMClient client, LCIMException e) {
        if (null != e) {
          System.out.println("User2 open failed.");
        }
        tmpCounter2.countDown();
      }
    });
    countDownLatch.await();
    assertTrue(opersationSucceed);
    Thread.sleep(3000);
  }

  public void testRecallMessage() throws Exception {
    final CountDownLatch tmpCounter = new CountDownLatch(1);
    client = LCIMClient.getInstance("testUser1");
    client.open(new LCIMClientCallback() {
      @Override
      public void done(LCIMClient client, LCIMException e) {
        tmpCounter.countDown();
      }
    });
    tmpCounter.await();
    client.createConversation(memebers, convName, null, false, true, new LCIMConversationCreatedCallback() {
      @Override
      public void done(final LCIMConversation conversation, LCIMException e) {
        if (null != e) {
          e.printStackTrace();
          countDownLatch.countDown();
        } else {
          final LCIMTextMessage msg = new LCIMTextMessage();
          msg.setText("test run @" + System.currentTimeMillis());
          conversation.sendMessage(msg, new LCIMConversationCallback() {
            @Override
            public void done(LCIMException ex) {
              if (null != ex) {
                System.out.println("failed to send message");
                ex.printStackTrace();
                countDownLatch.countDown();
              } else {
                System.out.println("succeed to send message");
                conversation.recallMessage(msg, new LCIMMessageRecalledCallback() {
                  @Override
                  public void done(LCIMRecalledMessage recalledMessage, LCException e) {
                    if (null != e) {
                      System.out.println("failed to recall message");
                    } else {
                      opersationSucceed = true;
                    }
                    countDownLatch.countDown();
                  }
                });
              }
            }
          });
        }
      }
    });
    countDownLatch.await();
    assertTrue(opersationSucceed);
  }

  public void testUpdateMessage() throws Exception {
    final CountDownLatch tmpCounter = new CountDownLatch(1);
    client = LCIMClient.getInstance("testUser1");
    client.open(new LCIMClientCallback() {
      @Override
      public void done(LCIMClient client, LCIMException e) {
        tmpCounter.countDown();
      }
    });
    tmpCounter.await();
    client.createConversation(memebers, convName, null, false, true, new LCIMConversationCreatedCallback() {
      @Override
      public void done(final LCIMConversation conversation, LCIMException e) {
        if (null != e) {
          e.printStackTrace();
          countDownLatch.countDown();
        } else {
          final LCIMTextMessage msg = new LCIMTextMessage();
          msg.setText("test run @" + System.currentTimeMillis());
          conversation.sendMessage(msg, new LCIMConversationCallback() {
            @Override
            public void done(LCIMException ex) {
              if (null != ex) {
                System.out.println("failed to send message, cause:" + ex.getMessage());
                ex.printStackTrace();
                countDownLatch.countDown();
              } else {
                System.out.println("succeed to send message. messageId:" + msg.getMessageId());
                LCIMTextMessage newMsg = new LCIMTextMessage();
                newMsg.setText("test updated @" + System.currentTimeMillis());
                conversation.updateMessage(msg, newMsg, new LCIMMessageUpdatedCallback() {
                  @Override
                  public void done(LCIMMessage curMessage, LCException e) {
                    if (null != e) {
                      System.out.println("failed to update message， cause:" + e.getMessage());
                    } else {
                      System.out.println("succeed to patch message");
                      opersationSucceed = true;
                    }
                    countDownLatch.countDown();
                  }
                });
              }
            }
          });
        }
      }
    });
    countDownLatch.await();
    assertTrue(opersationSucceed);
  }

  public void testUpdateMessageNotification() throws Exception {
    final CountDownLatch firstStage = new CountDownLatch(1);
    final CountDownLatch secondStage = new CountDownLatch(1);
    final CountDownLatch endStage = new CountDownLatch(1);

    Thread senderThread = new Thread(new Runnable() {
      LCIMConversation targetConversation;
      LCIMTextMessage targetMessage;
      @Override
      public void run() {
        final CountDownLatch tmpCounter = new CountDownLatch(1);

        client = LCIMClient.getInstance("testUser1");
        client.open(new LCIMClientCallback() {
          @Override
          public void done(LCIMClient client, LCIMException e) {
            if (null != e) {
              System.out.println("☑️testUser1 loggin...");
            }
            tmpCounter.countDown();
          }
        });
        try {
          tmpCounter.await();
        } catch (Exception ex) {
          ex.printStackTrace();
        }
        client.createConversation(memebers, convName, null, false, true, new LCIMConversationCreatedCallback() {
          @Override
          public void done(final LCIMConversation conversation, LCIMException e) {
            if (null != e) {
              e.printStackTrace();
              firstStage.countDown();
            } else {
              System.out.println("☑️☑️testUser1 join conversation:" + conversation.getConversationId() + "...");
              testConversationId = conversation.getConversationId();
              final LCIMTextMessage msg = new LCIMTextMessage();
              msg.setText("test run @" + System.currentTimeMillis());
              conversation.sendMessage(msg, new LCIMConversationCallback() {
                @Override
                public void done(LCIMException ex) {
                  if (null != ex) {
                    System.out.println("❌️testUser1 failed to send message, cause:" + ex.getMessage());
                    ex.printStackTrace();
                    firstStage.countDown();
                  } else {
                    System.out.println("☑️☑️☑️testUser1 succeed to send message. messageId:" + msg.getMessageId());
                    targetConversation = conversation;
                    targetMessage = msg;
                    firstStage.countDown();
                  }
                }
              });
            }
          }
        });
        try {
          secondStage.await();
        } catch (Exception ex) {
          ex.printStackTrace();
        }
        final CountDownLatch updateLatch = new CountDownLatch(1);
        LCIMTextMessage newMsg = new LCIMTextMessage();
        newMsg.setText("test updated @" + System.currentTimeMillis());
        targetConversation.updateMessage(targetMessage, newMsg, new LCIMMessageUpdatedCallback() {
          @Override
          public void done(LCIMMessage curMessage, LCException e) {
            if (null != e) {
              System.out.println("❌️testUser1 failed to update message， cause:" + e.getMessage());
            } else {
              System.out.println("☑️☑️☑️☑️testUser1 succeed to patch message");
            }
            updateLatch.countDown();
          }
        });
        try {
          updateLatch.await();
        } catch (Exception ex) {
          ex.printStackTrace();
        }
        endStage.countDown();
        System.out.println("Sender Thread exited.");
      }
    });
    Thread receiverThread = new Thread(new Runnable() {
      LCIMClient currentClient = LCIMClient.getInstance("User2");
      @Override
      public void run() {
        final CountDownLatch loginLatch = new CountDownLatch(1);
        currentClient.open(new LCIMClientCallback() {
          @Override
          public void done(LCIMClient client, LCIMException e) {
            if (null == e) {
              System.out.println("☑️User2 loggin...");
            }
            loginLatch.countDown();
          }
        });
        try {
          loginLatch.await();
        } catch (Exception ex) {
          ex.printStackTrace();
        }
        try {
          firstStage.await();
        } catch (Exception ex) {
          ex.printStackTrace();
        }

        if (!StringUtil.isEmpty(testConversationId)) {
          final LCIMConversation targetConversation = currentClient.getConversation(testConversationId);
          final CountDownLatch joinLatch = new CountDownLatch(1);
          targetConversation.join(new LCIMConversationCallback() {
            @Override
            public void done(LCIMException e) {
              if (null != e) {
                e.printStackTrace();
              } else {
                System.out.println("☑️☑️User2 join conversation: " + testConversationId + "...");
              }
              LCIMMessage lastMessage = targetConversation.getLastMessage();
              joinLatch.countDown();
            }
          });
          try {
            joinLatch.await();
          } catch (Exception ex) {
            ;
          }
          secondStage.countDown();
          System.out.println("☑️☑️User2 notify sender to update message...");

          try {
            endStage.await();
          } catch (Exception ex) {
            ;
          }
          final CountDownLatch updateLatch = new CountDownLatch(1);
          System.out.println("☑️☑️️User2 got notification and try to query message...");
          targetConversation.queryMessages(1, new LCIMMessagesQueryCallback() {
            @Override
            public void done(List<LCIMMessage> messages, LCIMException e) {
              if (null != e || null == messages || messages.size() < 1) {
                System.out.println("User2 failed to query messages. cause:" + e.getMessage());
                updateLatch.countDown();
              } else {
                System.out.println("☑️☑️☑️User2 try to update message...");
                LCIMMessage targetMessage = messages.get(0);
                LCIMTextMessage newMsg = new LCIMTextMessage();
                newMsg.setText("test updated @" + System.currentTimeMillis());
                targetConversation.updateMessage(targetMessage, newMsg, new LCIMMessageUpdatedCallback() {
                  @Override
                  public void done(LCIMMessage message, LCException e) {
                    if (null != e) {
                      e.printStackTrace();
                    }
                    System.out.println("☑️☑️☑️☑️User2 update message result: " + (null != e));
                    opersationSucceed = true;
                    currentClient.close(new LCIMClientCallback() {
                      @Override
                      public void done(LCIMClient client, LCIMException e) {
                        updateLatch.countDown();
                      }
                    });
                  }
                });
              }
            }
          });
          try {
            updateLatch.await();
          } catch (Exception ex) {
            ;
          }
        } else {
          secondStage.countDown();
        }
        System.out.println("Receiver Thread Exit!");
      }
    });

    senderThread.start();
    receiverThread.start();
    senderThread.join(10000);
    receiverThread.join(10000);
    assertTrue(opersationSucceed);
  }

  public void testQueryMessagesWithUser2() throws Exception {
    final LCIMClient client = LCIMClient.getInstance("User2");
    final CountDownLatch loginLatch = new CountDownLatch(1);
    client.open(new LCIMClientCallback() {
      @Override
      public void done(LCIMClient client, LCIMException e) {
        if (null == e) {
          System.out.println("☑️User2 loggin...");
        }
        loginLatch.countDown();
      }
    });
    try {
      loginLatch.await();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    testConversationId = "5ce23b8ec320f1ab6f53fd50";
    final LCIMConversation targetConversation = client.getConversation(testConversationId);
    final CountDownLatch joinLatch = new CountDownLatch(1);
    targetConversation.join(new LCIMConversationCallback() {
      @Override
      public void done(LCIMException e) {
        if (null != e) {
          e.printStackTrace();
        } else {
          System.out.println("☑️☑️User2 join conversation: " + testConversationId + "...");
        }
        LCIMMessage lastMessage = targetConversation.getLastMessage();
        joinLatch.countDown();
      }
    });
    try {
      joinLatch.await();
    } catch (Exception ex) {
      ;
    }

    final CountDownLatch updateLatch = new CountDownLatch(1);
    System.out.println("☑️☑️️User2 got notification and try to query message...");
    targetConversation.queryMessages(1, new LCIMMessagesQueryCallback() {
      @Override
      public void done(List<LCIMMessage> messages, LCIMException e) {
        if (null != e ) {
          System.out.println("User2 failed to query messages. cause:" + e.getMessage());
          updateLatch.countDown();
        } else if (null == messages || messages.size() < 1) {
          System.out.println("User2 failed to query messages. result: null");
          updateLatch.countDown();
        } else {
          System.out.println("☑️☑️☑️User2 try to update message...");
          LCIMMessage targetMessage = messages.get(0);
          LCIMTextMessage newMsg = new LCIMTextMessage();
          newMsg.setText("test updated @" + System.currentTimeMillis());
          targetConversation.updateMessage(targetMessage, newMsg, new LCIMMessageUpdatedCallback() {
            @Override
            public void done(LCIMMessage message, LCException e) {
              if (null != e) {
                e.printStackTrace();
              }
              System.out.println("☑️☑️☑️☑️User2 update message result: " + (null != e));
              opersationSucceed = true;
              updateLatch.countDown();
            }
          });
        }
      }
    });
    updateLatch.await();
    assertTrue(opersationSucceed);
  }

  public void testQueryMessages() throws Exception {
    final CountDownLatch tmpCounter = new CountDownLatch(1);
    client = LCIMClient.getInstance("testUser1");
    client.open(new LCIMClientCallback() {
      @Override
      public void done(LCIMClient client, LCIMException e) {
        tmpCounter.countDown();
      }
    });
    tmpCounter.await();
    client.createConversation(memebers, convName, null, false, true, new LCIMConversationCreatedCallback() {
      @Override
      public void done(LCIMConversation conversation, LCIMException e) {
        if (null != e) {
          e.printStackTrace();
          countDownLatch.countDown();
        } else {
          conversation.queryMessages(new LCIMMessagesQueryCallback() {
            @Override
            public void done(List<LCIMMessage> messages, LCIMException ex) {
              if (null != ex) {
                System.out.println("failed to query message");
                ex.printStackTrace();
              } else {
                System.out.println("succeed to query message: " + messages.size());
                opersationSucceed = true;
              }
              countDownLatch.countDown();
            }
          });
        }
      }
    });
    countDownLatch.await();
    assertTrue(opersationSucceed);
  }

  public void testJoinedNotification() throws Exception {
    client = LCIMClient.getInstance("testUser1");
    final CountDownLatch tmpCounter = new CountDownLatch(1);
    client.open(new LCIMClientCallback() {
      @Override
      public void done(LCIMClient client, LCIMException e) {
        tmpCounter.countDown();
      }
    });
    tmpCounter.await();
    Map<String, Object> attr = new HashMap<>();
    attr.put("testTs", System.currentTimeMillis());
    attr.put("owner", "testUser1");
    client.createConversation(memebers, convName, attr, false, false, new LCIMConversationCreatedCallback() {
      @Override
      public void done(LCIMConversation conversation, LCIMException e) {
        if (null != e) {
          e.printStackTrace();
          countDownLatch.countDown();
        } else {
          LCIMTextMessage msg = new LCIMTextMessage();
          msg.setText("test run @" + System.currentTimeMillis());
          conversation.sendMessage(msg, new LCIMConversationCallback() {
            @Override
            public void done(LCIMException ex) {
              if (null != ex) {
                System.out.println("failed to send message");
                ex.printStackTrace();
              } else {
                System.out.println("succeed to send message");
                opersationSucceed = true;
              }
              countDownLatch.countDown();
            }
          });
        }
      }
    });
    countDownLatch.await();
    assertTrue(opersationSucceed);
    Thread.sleep(2000);
  }

  public void testConversationQueryWithCache() throws Exception {
    client = LCIMClient.getInstance("testUser1");
    final CountDownLatch tmpCounter = new CountDownLatch(1);
    client.open(new LCIMClientCallback() {
      @Override
      public void done(LCIMClient client, LCIMException e) {
        tmpCounter.countDown();
      }
    });
    tmpCounter.await();

    LCIMConversationsQuery query = client.getConversationsQuery();
    query.containsMembers(Arrays.asList("testUser1"));
    query.addAscendingOrder("updatedAt");
    query.findInBackground(new LCIMConversationQueryCallback() {
      @Override
      public void done(List<LCIMConversation> conversations, LCIMException e) {
        if (null != e) {
          System.out.println("failed to query converstaion.");
        } else {
          System.out.println("succeed to query converstaion.");
          for (LCIMConversation conv: conversations) {
            System.out.println(conv);
          }
          // in core library, no db cache.
          opersationSucceed = true;
        }
        countDownLatch.countDown();
      }
    });
    countDownLatch.await();
    assertTrue(opersationSucceed);
  }

  public void testConversationQueryWithNetwork() throws Exception {
    client = LCIMClient.getInstance("testUser1");
    final CountDownLatch tmpCounter = new CountDownLatch(1);
    client.open(new LCIMClientCallback() {
      @Override
      public void done(LCIMClient client, LCIMException e) {
        tmpCounter.countDown();
      }
    });
    tmpCounter.await();

    LCIMConversationsQuery query = client.getConversationsQuery();
    query.containsMembers(Arrays.asList("testUser1"));
    query.addAscendingOrder("updatedAt");
    query.setQueryPolicy(LCQuery.CachePolicy.NETWORK_ELSE_CACHE);
    query.findInBackground(new LCIMConversationQueryCallback() {
      @Override
      public void done(List<LCIMConversation> conversations, LCIMException e) {
        if (null != e) {
          System.out.println("failed to query converstaion.");
        } else {
          System.out.println("succeed to query converstaion.");
          opersationSucceed = conversations.size() > 0;
          for(LCIMConversation c: conversations) {
            System.out.println(c.getType());
          }
        }
        countDownLatch.countDown();
      }
    });
    countDownLatch.await();
    assertTrue(opersationSucceed);
  }

  public void testDummyConversationEventHandlerCounter() throws Exception {
    LCIMClient client = LCIMClient.getInstance("testUser");
    LCIMConversation conv = client.getConversation("conversationId");
    this.conversationEventHandler.onBlocked(client, conv, "nobody");
    this.conversationEventHandler.onInvited(client, conv, "nobody");
    this.conversationEventHandler.onMemberBlocked(client, conv, Arrays.asList("Tom", "Jerry"),"nobody");
    assertTrue(this.conversationEventHandler.getCount(0x00ffFF) == 3);
  }

  public void testCreateTempConversation() throws Exception {
    client = LCIMClient.getInstance("testUser1");
    final CountDownLatch tmpCounter = new CountDownLatch(1);
    client.open(new LCIMClientCallback() {
      @Override
      public void done(LCIMClient client, LCIMException e) {
        tmpCounter.countDown();
      }
    });
    tmpCounter.await();
    client.createTemporaryConversation(Arrays.asList("testUser007"), 1800, new LCIMConversationCreatedCallback() {
      @Override
      public void done(LCIMConversation conversation, LCIMException e) {
        if (null != e) {
          System.out.println("failed to create temp conversation. cause:" + e.getMessage());
          countDownLatch.countDown();
        } else {
          System.out.println("succeed to create temp conversation: " + conversation.toJSONString());
          client.getConversationsQuery().findTempConversationsInBackground(Arrays.asList(conversation.getConversationId()),
                  new LCIMConversationQueryCallback() {
            @Override
            public void done(List<LCIMConversation> conversations, LCIMException e) {
              if (null != e) {
                System.out.println("failed to create conversation. cause:" + e.getMessage());
                countDownLatch.countDown();
              } else {
                for (LCIMConversation c : conversations) {
                  System.out.println(c.toJSONString());
                }
                opersationSucceed = conversations.size() > 0;
                countDownLatch.countDown();
              }
            }
          });
        }
      }
    });
    countDownLatch.await();
    assertTrue(opersationSucceed);
  }

  public void testCreateConversationWithAttributes() throws Exception {
    client = LCIMClient.getInstance("testUser1");
    final CountDownLatch tmpCounter = new CountDownLatch(1);
    client.open(new LCIMClientCallback() {
      @Override
      public void done(LCIMClient client, LCIMException e) {
        tmpCounter.countDown();
      }
    });
    tmpCounter.await();
    final String convName = "TestConv-" + System.currentTimeMillis();
    final Map<String, Object> attributes = new HashMap<>();
    attributes.put("type", 3);
    attributes.put("ts", System.currentTimeMillis());
    client.createConversation(Arrays.asList("testUser007"), convName, attributes, true, true, new LCIMConversationCreatedCallback() {
      @Override
      public void done(LCIMConversation conversation, LCIMException e) {
        if (null != e) {
          System.out.println("failed to create conversation. cause:" + e.getMessage());
          countDownLatch.countDown();
        } else {
          System.out.println("succeed to create conversation: " + conversation.toJSONString());
          String name = conversation.getName();
          int type = (int)conversation.getAttribute("type");
          System.out.println("created. name=" + name + ", type=" + type + ", uniqueId=" + conversation.getUniqueId());
          opersationSucceed = convName.equals(name) && 3 == type;
          countDownLatch.countDown();
        }
      }
    });
    countDownLatch.await();
    assertTrue(opersationSucceed);
  }

  public void testCreateUniqueConversationWithAttributes() throws Exception {
    client = LCIMClient.getInstance("testUser1");
    final CountDownLatch tmpCounter = new CountDownLatch(1);
    client.open(new LCIMClientCallback() {
      @Override
      public void done(LCIMClient client, LCIMException e) {
        tmpCounter.countDown();
      }
    });
    tmpCounter.await();
    final String convName = "TestConv-" + System.currentTimeMillis();
    final Map<String, Object> attributes = new HashMap<>();
    attributes.put("type", 3);
    attributes.put("ts", System.currentTimeMillis());
    client.createConversation(Arrays.asList("testUser007"), convName, attributes, false, true, new LCIMConversationCreatedCallback() {
      @Override
      public void done(LCIMConversation conversation, LCIMException e) {
        if (null != e) {
          System.out.println("failed to create conversation. cause:" + e.getMessage());
          countDownLatch.countDown();
        } else {
          System.out.println("succeed to create conversation: " + conversation.toJSONString());
          String name = conversation.getName();
          int type = (int)conversation.getAttribute("type");
          System.out.println("created. name=" + name + ", type=" + type + ", uniqueId=" + conversation.getUniqueId());
          opersationSucceed = convName.equals(name) && 3 == type && conversation.isUnique();
          countDownLatch.countDown();
        }
      }
    });
    countDownLatch.await();
    assertTrue(opersationSucceed);
  }

  public void testConversationJoinAndQuit() throws Exception {
    client = LCIMClient.getInstance("testUser1");
    final CountDownLatch tmpCounter = new CountDownLatch(1);
    client.open(new LCIMClientCallback() {
      @Override
      public void done(LCIMClient client, LCIMException e) {
        tmpCounter.countDown();
      }
    });
    tmpCounter.await();
    String conversationId = "5dee02017c4cc935c85c93de";
    final LCIMConversation conversation = client.getConversation(conversationId, true, false);
    conversation.join(new LCIMConversationCallback() {
      @Override
      public void done(LCIMException e) {
        if (null != e) {
          System.out.println("failed to join conversation. cause:" + e.getMessage());
          countDownLatch.countDown();
        } else {
          System.out.println("succeed to join conversation.");
          try {
            Thread.sleep(1000);
          } catch (Exception ex) {
            ex.printStackTrace();
          }
          conversation.quit(new LCIMConversationCallback() {
            @Override
            public void done(LCIMException e) {
              if (null != e) {
                System.out.println("failed to quit conversation. cause:" + e.getMessage());
              } else {
                System.out.println("succeed to quit conversation.");
                opersationSucceed = true;
              }
              countDownLatch.countDown();
            }
          });
        }
      }
    });
    countDownLatch.await();
    assertTrue(opersationSucceed);
  }

  public void testConversationRead() throws Exception {
    client = LCIMClient.getInstance("testUser1");
    final CountDownLatch tmpCounter = new CountDownLatch(1);
    client.open(new LCIMClientCallback() {
      @Override
      public void done(LCIMClient client, LCIMException e) {
        tmpCounter.countDown();
      }
    });
    tmpCounter.await();
    String conversationId = "5dee02017c4cc935c85c93de";
    final LCIMConversation conversation = client.getConversation(conversationId, false, false);
    conversation.join(new LCIMConversationCallback() {
      @Override
      public void done(LCIMException e) {
        if (null != e) {
          System.out.println("failed to join conversation. cause: " + e.getMessage());
          countDownLatch.countDown();
        } else {
          System.out.println("succeed to join conversation");
          conversation.read();
          try {
            Thread.sleep(1000);
          } catch (Exception ex) {
            ex.printStackTrace();
          }
          conversation.quit(new LCIMConversationCallback() {
            @Override
            public void done(LCIMException e) {
              if (null != e) {
                System.out.println("failed to quit conversation. cause:" + e.getMessage());
              } else {
                System.out.println("succeed to quit conversation.");
                opersationSucceed = true;
              }
              countDownLatch.countDown();
            }
          });
        }
      }
    });
    countDownLatch.await();
    assertTrue(opersationSucceed);
  }

  public void testConversationFetch() throws Exception {
    client = LCIMClient.getInstance("testUser1");
    final CountDownLatch tmpCounter = new CountDownLatch(1);
    client.open(new LCIMClientCallback() {
      @Override
      public void done(LCIMClient client, LCIMException e) {
        tmpCounter.countDown();
      }
    });
    tmpCounter.await();
    String conversationId = "5dee02017c4cc935c85c93de";
    LCIMConversation conversation = client.getConversation(conversationId, false, false);
    conversation.fetchInfoInBackground(new LCIMConversationCallback() {
      @Override
      public void done(LCIMException e) {
        if (null != e) {
          System.out.println("failed to fetch info. cause: " + e.getMessage());
        } else {
          System.out.println("succeed to fetch info.");
          opersationSucceed = true;
        }
        countDownLatch.countDown();
      }
    });
    countDownLatch.await();
    assertTrue(opersationSucceed);
  }

  public void testConversationQueryMemberCount() throws Exception {
    client = LCIMClient.getInstance("testUser1");
    final CountDownLatch tmpCounter = new CountDownLatch(1);
    client.open(new LCIMClientCallback() {
      @Override
      public void done(LCIMClient client, LCIMException e) {
        tmpCounter.countDown();
      }
    });
    tmpCounter.await();
    String conversationId = "5dee02017c4cc935c85c93de";
    System.out.println("begin to fetch conversation:" + conversationId);
    final LCIMConversation conversation = client.getConversation(conversationId, false, false);
    conversation.fetchInfoInBackground(new LCIMConversationCallback() {
      @Override
      public void done(LCIMException e) {
        if (null != e) {
          System.out.println("failed to refresh convesation.");
          e.printStackTrace();
          countDownLatch.countDown();
        } else {
          System.out.println("succeed fetch conversation:" + conversation.toJSONString());
          conversation.getMemberCount(new LCIMConversationMemberCountCallback() {
            @Override
            public void done(Integer memberCount, LCIMException e) {
              if (null != e) {
                e.printStackTrace();
              } else {
                System.out.println("get member count: " + memberCount);
              }
              opersationSucceed = e == null;
              countDownLatch.countDown();
            }
          });
        }
      }
    });
    countDownLatch.await();
    assertTrue(opersationSucceed);
  }

  public void testModifyAndDeleteAttributes() throws Exception {
    client = LCIMClient.getInstance("William");
    final CountDownLatch tmpCounter = new CountDownLatch(1);
    client.open(new LCIMClientCallback() {
      @Override
      public void done(LCIMClient client, LCIMException e) {
        System.out.println("client open finished.");
        tmpCounter.countDown();
      }
    });
    tmpCounter.await();
    String conversationId = "5dee02017c4cc935c85c93de";
    final LCIMConversation conversation = client.getConversation(conversationId, false, false);
    conversation.fetchInfoInBackground(new LCIMConversationCallback() {
      @Override
      public void done(LCIMException e) {
        if (null != e) {
          System.out.println("failed to fetch Conversation. cause: " + e.getMessage());
          countDownLatch.countDown();
        } else {
          final String convName = conversation.getName();
          final String convAttrName = (String) conversation.getAttribute("name");
          System.out.println("convName=" + convName + ", convAttrName=" + convAttrName);
          final long now = System.currentTimeMillis();

          conversation.setName("TestConv" + now);
          conversation.setAttribute("ts", now);
          conversation.set("attr.type", 4);

          conversation.updateInfoInBackground(new LCIMConversationCallback() {
            @Override
            public void done(LCIMException e) {
              if (null == e) {
                boolean verifiedOnFirstUpdate = (int)conversation.get("attr.type") == 4
                        && conversation.getName().equals("TestConv" + now)
                        && (long)conversation.getAttribute("ts") == now;
                if (!verifiedOnFirstUpdate) {
                  System.out.println("failed to verify conversation attribute after first updated. conv=" + conversation.toJSONString());
                  countDownLatch.countDown();
                } else {
                  System.out.println("conversation attribute after first updated. conv=" + conversation.toJSONString());
                  conversation.remove("attr.ts");
                  conversation.remove("attr.type");
                  conversation.setName(convName);
                  conversation.updateInfoInBackground(new LCIMConversationCallback() {
                    @Override
                    public void done(LCIMException e) {
                      if (null == e) {
                        opersationSucceed = null == conversation.get("attr.ts")
                                && conversation.getName().equals(convName)
                                && null == conversation.get("attr.type");
                        if (!opersationSucceed) {
                          System.out.println("failed to verify conversation attribute after second updated. conv=" + conversation.toJSONString());
                        } else {
                          System.out.println("conversation attribute after second updated. conv=" + conversation.toJSONString());
                        }
                      }
                      countDownLatch.countDown();
                    }
                  });
                }
              } else {
                e.printStackTrace();
                countDownLatch.countDown();
              }

            }
          });
        }
      }
    });

    countDownLatch.await();
    assertTrue(opersationSucceed);
  }

  public void testConversationUpdateName() throws Exception {
    client = LCIMClient.getInstance("William");
    final CountDownLatch tmpCounter = new CountDownLatch(1);
    client.open(new LCIMClientCallback() {
      @Override
      public void done(LCIMClient client, LCIMException e) {
        System.out.println("client open finished.");
        tmpCounter.countDown();
      }
    });
    tmpCounter.await();
    String conversationId = "5dee02017c4cc935c85c93de";
    final LCIMConversation conversation = client.getConversation(conversationId, false, false);
    conversation.fetchInfoInBackground(new LCIMConversationCallback() {
      @Override
      public void done(LCIMException e) {
        if (null != e) {
          System.out.println("failed to fetch Conversation. cause: " + e.getMessage());
          countDownLatch.countDown();
        } else {
          String convName = conversation.getName();
          String convAttrName = (String) conversation.getAttribute("name");
          System.out.println("convName=" + convName + ", convAttrName=" + convAttrName);

          conversation.setName("TestConv" + System.currentTimeMillis());
          conversation.setAttribute("ts", System.currentTimeMillis());
          conversation.set("attr.type", 4);

          conversation.updateInfoInBackground(new LCIMConversationCallback() {
            @Override
            public void done(LCIMException e) {
              if (null == e) {
                opersationSucceed = true;
              } else {
                e.printStackTrace();
              }
              countDownLatch.countDown();
            }
          });
        }
      }
    });

    countDownLatch.await();
    assertTrue(opersationSucceed);
  }

  public void testConversationFetchLastTime() throws Exception {
    client = LCIMClient.getInstance("testUser1");
    final CountDownLatch tmpCounter = new CountDownLatch(1);
    client.open(new LCIMClientCallback() {
      @Override
      public void done(LCIMClient client, LCIMException e) {
        tmpCounter.countDown();
      }
    });
    tmpCounter.await();
    String conversationId = "5805eefd8159ccabfc39bc1c";
    final LCIMConversation conversation = client.getConversation(conversationId, false, false);
    conversation.fetchReceiptTimestamps(new LCIMConversationCallback() {
      @Override
      public void done(LCIMException e) {
        if (null != e) {
          System.out.println("failed to fetch ReceiptTimestamps. cause: " + e.getMessage());
          opersationSucceed = true;
        } else {
          System.out.println("succeed to fetch ReceiptTimestamps.");
          System.out.println("LastReadAt: " + conversation.getLastReadAt());
          System.out.println("LastDeliveredAt: " + conversation.getLastDeliveredAt());
        }
        countDownLatch.countDown();
      }
    });
    countDownLatch.await();
    assertTrue(opersationSucceed);
  }

  public void testConversationFetchLastTimeWithMembership() throws Exception {
    client = LCIMClient.getInstance("testUser1");
    final CountDownLatch tmpCounter = new CountDownLatch(1);
    client.open(new LCIMClientCallback() {
      @Override
      public void done(LCIMClient client, LCIMException e) {
        tmpCounter.countDown();
      }
    });
    tmpCounter.await();
    String conversationId = "5dee02017c4cc935c85c93de";
    System.out.println("begin to join converseration: " + conversationId);
    final LCIMConversation conversation = client.getConversation(conversationId, false, false);
    conversation.join(new LCIMConversationCallback() {
      @Override
      public void done(LCIMException e) {
        if (null != e) {
          System.out.println("failed to join conversation. cause:" + e.getMessage());
          countDownLatch.countDown();
        } else {
          System.out.println("succeed to join to conversation: " + conversation.toJSONString());
          conversation.fetchReceiptTimestamps(new LCIMConversationCallback() {
            @Override
            public void done(LCIMException e) {
              if (null != e) {
                System.out.println("failed to fetch ReceiptTimestamps. cause: " + e.getMessage());
                countDownLatch.countDown();
              } else {
                System.out.println("succeed to fetch ReceiptTimestamps." + conversation.toJSONString());
                System.out.println("LastReadAt: " + conversation.getLastReadAt());
                System.out.println("LastDeliveredAt: " + conversation.getLastDeliveredAt());
                conversation.quit(new LCIMConversationCallback() {
                  @Override
                  public void done(LCIMException e) {
                    if (null != e) {
                      System.out.println("failed to quit conversation. cause:" + e.getMessage());
                    } else {
                      opersationSucceed = true;
                    }
                    countDownLatch.countDown();
                  }
                });
              }
            }
          });
        }
      }
    });

    countDownLatch.await();
    assertTrue(opersationSucceed);
  }

  public void testAttributeGetter() throws Exception {
    String jsonString = "{\"unique\":true,\"updatedAt\":\"2020-01-21T14:22:48.642Z\",\"name\":\"TestConv1579603439430\"," +
            "\"objectId\":\"5dee02017c4cc935c85c93de\",\"m\":[\"T8\",\"William\"],\"tr\":false,\"createdAt\":\"2019-12-09T08:12:49.475Z\"," +
            "\"c\":\"T8\",\"uniqueId\":\"61e52ce97f2b93050655a1c83371b070\",\"mu\":[]," +
            "\"attr\":{\"alias\":\"TestConv1575943063325\",\"type\":4,\"ts\":1579603439430}}";
    Map<String, Object> jsonObj = JSON.parseObject(jsonString, Map.class);
    LCIMClient client = LCIMClient.getInstance("test");
    LCIMConversation conv = LCIMConversation.parseFromJson(client, jsonObj);
    Date createdAt = StringUtil.dateFromString("2019-12-09T08:12:49.475Z");
    Date updatedAt = StringUtil.dateFromString("2020-01-21T14:22:48.642Z");
    assertTrue(conv.isUnique());
    assertTrue(!conv.isSystem());
    assertTrue(!conv.isTransient());
    assertTrue(!conv.isTemporary());
    assertTrue(conv.getTemporaryExpiredat() == 0);
    assertTrue(conv.getName().equals("TestConv1579603439430"));
    assertTrue(conv.getConversationId().equals("5dee02017c4cc935c85c93de"));
    assertTrue(conv.getCreator().equals("T8"));
    assertTrue(conv.getLastMessage() == null);
    assertTrue(conv.getUniqueId().equals("61e52ce97f2b93050655a1c83371b070"));
    assertTrue(conv.getCreatedAt().getTime() == createdAt.getTime());
    assertTrue(conv.getUpdatedAt().getTime() == updatedAt.getTime());

    assertTrue(conv.getAttribute("name").equals("TestConv1579603439430"));
    assertTrue(conv.getAttribute("alias").equals("TestConv1575943063325"));
    assertTrue((int)conv.getAttribute("type") == 4);
    assertTrue((long)conv.getAttribute("attr.ts") == 1579603439430l);

    assertTrue(conv.get("name").equals("TestConv1579603439430"));
    assertTrue(conv.get("attr.alias").equals("TestConv1575943063325"));
    assertTrue((int)conv.get("attr.type") == 4);
    assertTrue((long)conv.get("attr.ts") == 1579603439430l);

    assertNull(conv.getAttribute("nothing"));
    assertNull(conv.get("nothing"));
    assertNull(conv.get("attr.nothing"));
    assertNull(conv.get("attr.nothing.ts"));
  }

  public void testMuteConversation() throws Exception {
    client = LCIMClient.getInstance("testUser1");
    final CountDownLatch tmpCounter = new CountDownLatch(1);
    client.open(new LCIMClientCallback() {
      @Override
      public void done(LCIMClient client, LCIMException e) {
        if (null != e) {
          e.printStackTrace();
        }
        System.out.println("open client finished.");
        tmpCounter.countDown();
      }
    });
    tmpCounter.await();
    String conversationId = "5dee02017c4cc935c85c93de";
    System.out.println("begin to join conversation:" + conversationId);
    final LCIMConversation conversation = client.getConversation(conversationId, false, false);
    conversation.join(new LCIMConversationCallback() {
      @Override
      public void done(LCIMException e) {
        if (null != e) {
          System.out.println("failed to join conversation. cause: " + e.getMessage());
          countDownLatch.countDown();
        } else {
          System.out.println("succeed to join conversation：" + conversation.toJSONString());
          conversation.mute(new LCIMConversationCallback() {
            @Override
            public void done(LCIMException e) {
              if (null != e) {
                System.out.println("failed to mute conversation. cause:" + e.getMessage());
                countDownLatch.countDown();
              } else {
                System.out.println("succeed to mute conversation.");
                conversation.unmute(new LCIMConversationCallback() {
                  @Override
                  public void done(LCIMException e) {
                    if (null != e) {
                      System.out.println("failed to unmute conversation. cause:" + e.getMessage());
                      countDownLatch.countDown();
                    } else {
                      System.out.println("succeed to unmute conversation.");
                      conversation.quit(new LCIMConversationCallback() {
                        @Override
                        public void done(LCIMException e) {
                          if (null != e) {
                            System.out.println("failed to quit conversation. cause:" + e.getMessage());
                          } else {
                            System.out.println("succeed to quit conversation.");
                            opersationSucceed = true;
                          }
                          countDownLatch.countDown();
                        }
                      });
                    }
                  }
                });
              }
            }
          });
        }
      }
    });
    countDownLatch.await();
    assertTrue(opersationSucceed);
  }

  public void testMuteConversationMembers() throws Exception {
    client = LCIMClient.getInstance("testUser1");
    final CountDownLatch tmpCounter = new CountDownLatch(1);
    client.open(new LCIMClientCallback() {
      @Override
      public void done(LCIMClient client, LCIMException e) {
        tmpCounter.countDown();
      }
    });
    tmpCounter.await();
    List<String> members = Arrays.asList("testUser2", "testUser3", "testUser4");
    final List<String> blockMembers = Arrays.asList("testUser3", "testUser4");
    client.createConversation(members, "UnitTestConversation", null, false, true, new LCIMConversationCreatedCallback() {
      @Override
      public void done(final LCIMConversation conversation, LCIMException e) {
        if (null != e) {
          System.out.println("failed to create conversation. cause: " + e.getMessage());
          countDownLatch.countDown();
        } else {
          System.out.println("succeed to create conversation.");
          conversation.muteMembers(blockMembers, new LCIMOperationPartiallySucceededCallback() {
            @Override
            public void done(LCIMException e, List<String> successfulClientIds, List<LCIMOperationFailure> failures) {
              if (null != e) {
                System.out.println("failed to mute conversation members. cause: " + e.getMessage());
                countDownLatch.countDown();
              } else {
                System.out.println("succeed to mute conversation members.");
                conversation.queryMutedMembers(0, 100, new LCIMConversationSimpleResultCallback() {
                  @Override
                  public void done(List<String> memberIdList, LCIMException e) {
                    if (null != e) {
                      System.out.println("failed to queryMutedMembers. cause: " + e.getMessage());
                      e.printStackTrace();
                    } else {
                      System.out.println("succeed to queryMutedMembers.");
                    }
                    conversation.unmuteMembers(blockMembers, new LCIMOperationPartiallySucceededCallback() {
                      @Override
                      public void done(LCIMException e, List<String> successfulClientIds, List<LCIMOperationFailure> failures) {
                        if (null != e) {
                          System.out.println("failed to mute conversation members. cause: " + e.getMessage());
                        } else {
                          System.out.println("succeed to unmute conversation members.");
                          opersationSucceed = true;
                        }
                        countDownLatch.countDown();
                      }
                    });
                  }
                });
              }
            }
          });
        }
      }
    });
    countDownLatch.await();
    assertTrue(opersationSucceed);
  }

  public void testMuteConversationMembersV2() throws Exception {
    client = LCIMClient.getInstance("testUser1");
    final CountDownLatch tmpCounter = new CountDownLatch(1);
    client.open(new LCIMClientCallback() {
      @Override
      public void done(LCIMClient client, LCIMException e) {
        tmpCounter.countDown();
      }
    });
    tmpCounter.await();
    List<String> members = Arrays.asList("testUser2", "testUser3", "testUser4");
    final List<String> blockMembers = Arrays.asList("testUser3", "testUser4");
    client.createConversation(members, "UnitTestConversation", null, false, true, new LCIMConversationCreatedCallback() {
      @Override
      public void done(final LCIMConversation conversation, LCIMException e) {
        if (null != e) {
          System.out.println("failed to create conversation. cause: " + e.getMessage());
          countDownLatch.countDown();
        } else {
          System.out.println("succeed to create conversation.");
          conversation.muteMembers(blockMembers, new LCIMOperationPartiallySucceededCallback() {
            @Override
            public void done(LCIMException e, List<String> successfulClientIds, List<LCIMOperationFailure> failures) {
              if (null != e) {
                System.out.println("failed to mute conversation members. cause: " + e.getMessage());
                countDownLatch.countDown();
              } else {
                System.out.println("succeed to mute conversation members.");
                conversation.queryMutedMembers(100, null, new LCIMConversationIterableResultCallback() {
                  @Override
                  public void done(LCIMConversationIterableResult memberIdList, LCIMException e) {
                    if (null != e) {
                      System.out.println("failed to queryMutedMembers. cause: " + e.getMessage());
                      e.printStackTrace();
                    } else {
                      System.out.println("succeed to queryMutedMembers.");
                    }
                    conversation.unmuteMembers(blockMembers, new LCIMOperationPartiallySucceededCallback() {
                      @Override
                      public void done(LCIMException e, List<String> successfulClientIds, List<LCIMOperationFailure> failures) {
                        if (null != e) {
                          System.out.println("failed to mute conversation members. cause: " + e.getMessage());
                        } else {
                          System.out.println("succeed to unmute conversation members.");
                          opersationSucceed = true;
                        }
                        countDownLatch.countDown();
                      }
                    });
                  }
                });
              }
            }
          });
        }
      }
    });
    countDownLatch.await();
    assertTrue(opersationSucceed);
  }

  public void testBlockConversationMembers() throws Exception {
    client = LCIMClient.getInstance("testUser1");
    final CountDownLatch tmpCounter = new CountDownLatch(1);
    client.open(new LCIMClientCallback() {
      @Override
      public void done(LCIMClient client, LCIMException e) {
        tmpCounter.countDown();
      }
    });
    tmpCounter.await();
    List<String> members = Arrays.asList("testUser2", "testUser3", "testUser4");
    final List<String> blockMembers = Arrays.asList("testUser3", "testUser4");
    client.createConversation(members, "UnitTestConversation", null, false, true, new LCIMConversationCreatedCallback() {
      @Override
      public void done(final LCIMConversation conversation, LCIMException e) {
        if (null != e) {
          System.out.println("failed to create conversation. cause: " + e.getMessage());
          countDownLatch.countDown();
        } else {
          System.out.println("succeed to create conversation.");
          conversation.blockMembers(blockMembers, new LCIMOperationPartiallySucceededCallback() {
            @Override
            public void done(LCIMException e, List<String> successfulClientIds, List<LCIMOperationFailure> failures) {
              if (null != e) {
                System.out.println("failed to mute conversation members. cause: " + e.getMessage());
                countDownLatch.countDown();
              } else {
                System.out.println("succeed to mute conversation members.");
                conversation.queryBlockedMembers(0, 100, new LCIMConversationSimpleResultCallback() {
                  @Override
                  public void done(List<String> memberIdList, LCIMException e) {
                    if (null != e) {
                      System.out.println("failed to queryMutedMembers. cause: " + e.getMessage());
                      e.printStackTrace();
                    } else {
                      System.out.println("succeed to queryMutedMembers.");
                    }
                    conversation.unblockMembers(blockMembers, new LCIMOperationPartiallySucceededCallback() {
                      @Override
                      public void done(LCIMException e, List<String> successfulClientIds, List<LCIMOperationFailure> failures) {
                        if (null != e) {
                          System.out.println("failed to mute conversation members. cause: " + e.getMessage());
                        } else {
                          System.out.println("succeed to unmute conversation members.");
                          opersationSucceed = true;
                        }
                        countDownLatch.countDown();
                      }
                    });
                  }
                });
              }
            }
          });
        }
      }
    });
    countDownLatch.await();
    assertTrue(opersationSucceed);
  }

  public void testBlockConversationMembersV2() throws Exception {
    client = LCIMClient.getInstance("testUser1");
    final CountDownLatch tmpCounter = new CountDownLatch(1);
    client.open(new LCIMClientCallback() {
      @Override
      public void done(LCIMClient client, LCIMException e) {
        tmpCounter.countDown();
      }
    });
    tmpCounter.await();
    List<String> members = Arrays.asList("testUser2", "testUser3", "testUser4");
    final List<String> blockMembers = Arrays.asList("testUser3", "testUser4");
    client.createConversation(members, "UnitTestConversation", null, false, true, new LCIMConversationCreatedCallback() {
      @Override
      public void done(final LCIMConversation conversation, LCIMException e) {
        if (null != e) {
          System.out.println("failed to create conversation. cause: " + e.getMessage());
          countDownLatch.countDown();
        } else {
          System.out.println("succeed to create conversation.");
          conversation.blockMembers(blockMembers, new LCIMOperationPartiallySucceededCallback() {
            @Override
            public void done(LCIMException e, List<String> successfulClientIds, List<LCIMOperationFailure> failures) {
              if (null != e) {
                System.out.println("failed to block conversation members. cause: " + e.getMessage());
                countDownLatch.countDown();
              } else {
                System.out.println("succeed to block conversation members.");
                conversation.queryBlockedMembers(100, null, new LCIMConversationIterableResultCallback() {
                  @Override
                  public void done(LCIMConversationIterableResult memberIdList, LCIMException e) {
                    if (null != e) {
                      System.out.println("failed to queryBlockedMembers. cause: " + e.getMessage());
                      e.printStackTrace();
                    } else {
                      System.out.println("succeed to queryBlockedMembers.");
                    }
                    conversation.unblockMembers(blockMembers, new LCIMOperationPartiallySucceededCallback() {
                      @Override
                      public void done(LCIMException e, List<String> successfulClientIds, List<LCIMOperationFailure> failures) {
                        if (null != e) {
                          System.out.println("failed to unblock conversation members. cause: " + e.getMessage());
                        } else {
                          System.out.println("succeed to unblock conversation members.");
                          opersationSucceed = true;
                        }
                        countDownLatch.countDown();
                      }
                    });
                  }
                });
              }
            }
          });
        }
      }
    });
    countDownLatch.await();
    assertTrue(opersationSucceed);
  }

  public void testMuteConversationMembersInfo() throws Exception {
    client = LCIMClient.getInstance("testUser1");
    final CountDownLatch tmpCounter = new CountDownLatch(1);
    client.open(new LCIMClientCallback() {
      @Override
      public void done(LCIMClient client, LCIMException e) {
        tmpCounter.countDown();
      }
    });
    tmpCounter.await();
    List<String> members = Arrays.asList("testUser2", "testUser3", "testUser4");
    client.createConversation(members, "UnitTestConversation", null, false, true, new LCIMConversationCreatedCallback() {
      @Override
      public void done(final LCIMConversation conversation, LCIMException e) {
        if (null != e) {
          System.out.println("failed to create conversation. cause: " + e.getMessage());
          countDownLatch.countDown();
        } else {
          System.out.println("succeed to create conversation, continue to update member role...");
          conversation.updateMemberRole("testUser2", ConversationMemberRole.MANAGER, new LCIMConversationCallback() {
            @Override
            public void done(LCIMException e1) {
              if (null != e1) {
                System.out.println("failed to promote testUser2. cause: " + e1.getMessage());
                countDownLatch.countDown();
              } else {
                System.out.println("succeed to promote testUser2.");
                conversation.updateMemberRole("testUser3", ConversationMemberRole.MEMBER, new LCIMConversationCallback() {
                  @Override
                  public void done(LCIMException e2) {
                    if (null != e2) {
                      System.out.println("failed to promote testUser3. cause: " + e2.getMessage());
                      countDownLatch.countDown();
                    } else {
                      System.out.println("succeed to promote testUser3.");
                      conversation.getAllMemberInfo(0, 10, new LCIMConversationMemberQueryCallback() {
                        @Override
                        public void done(List<LCIMConversationMemberInfo> memberInfoList, LCIMException e3) {
                          if (null != e3) {
                            opersationSucceed = true;
                            if (null != memberInfoList) {
                              for (LCIMConversationMemberInfo info: memberInfoList) {
                                System.out.println("memberInfo: " + info.toString());
                              }
                            }
                          } else {
                            System.out.println("failed to query memberInfo. cause: " + e3.getMessage());
                          }
                          countDownLatch.countDown();
                        }
                      });
                    }
                  }
                });
              }
            }
          });
        }
      }
    });
    countDownLatch.await();
    assertTrue(opersationSucceed);
  }

  public void testSendAndReceiveMessage() throws Exception {
    final String senderId = "sender-" + System.currentTimeMillis();
    Thread.sleep(1000);
    final String receiverId = "receiver-" + System.currentTimeMillis();

    final CountDownLatch receierOnlineLatch = new CountDownLatch(1);

    Runnable sendThread = new Runnable() {
      @Override
      public void run() {
        final CountDownLatch tmpCounter = new CountDownLatch(1);
        final LCIMClient client = LCIMClient.getInstance(senderId);
        client.open(new LCIMClientCallback() {
          @Override
          public void done(final LCIMClient client, LCIMException e) {
            if (null != e) {
              System.out.println("failed to open sender client.");
              e.printStackTrace();
              tmpCounter.countDown();
            } else {
              System.out.println(client.getClientId() + " logged in, waiting for receiver online...");
            }
          }
        });
        try {
          receierOnlineLatch.await();
        } catch (Exception ex) {
          ex.printStackTrace();
        }
        client.createConversation(Arrays.asList(receiverId), null, null, false, true,
                new LCIMConversationCreatedCallback() {
                  @Override
                  public void done(final LCIMConversation conversation, LCIMException e) {
                    if (null != e) {
                      System.out.println("failed to create conversation from sender client.");
                      e.printStackTrace();
                      tmpCounter.countDown();
                    } else {
                      conversation.blockMembers(Arrays.asList("blockedUser"), new LCIMOperationPartiallySucceededCallback() {
                        @Override
                        public void done(LCIMException e, List<String> successfulClientIds, List<LCIMOperationFailure> failures) {
                          if (null != e) {
                            System.out.println("failed to block members from sender client.");
                            tmpCounter.countDown();
                          } else {
                            LCIMTextMessage msg = new LCIMTextMessage();
                            msg.setText("try to unblock user. @" + System.currentTimeMillis());
                            conversation.sendMessage(msg, new LCIMConversationCallback() {
                              @Override
                              public void done(LCIMException e) {
                                if (null != e) {
                                  System.out.println("failed to send message from sender client.");
                                  tmpCounter.countDown();
                                } else {
                                  conversation.unblockMembers(Arrays.asList("blockedUser"), new LCIMOperationPartiallySucceededCallback() {
                                    @Override
                                    public void done(LCIMException e, List<String> successfulClientIds, List<LCIMOperationFailure> failures) {
                                      if (null != e) {
                                        System.out.println("failed to unblock members from sender client.");
                                        e.printStackTrace();
                                      }
                                      client.close(new LCIMClientCallback() {
                                        @Override
                                        public void done(LCIMClient client, LCIMException e) {
                                          if (null != e) {
                                            System.out.println("failed to close sender client.");
                                          } else {
                                            System.out.println("succeed to run all flow on sender side.");
                                          }
                                          tmpCounter.countDown();
                                        }
                                      });
                                    }
                                  });
                                }
                              }
                            });

                          }
                        }
                      });
                    }
                  }
                });
        try {
          tmpCounter.await();
        } catch (Exception ex) {
          ex.printStackTrace();
        }
      }
    };
    Runnable receiveThread = new Runnable() {
      @Override
      public void run() {
        final CountDownLatch tmpCounter = new CountDownLatch(1);
        LCIMClient client = LCIMClient.getInstance(receiverId);
        client.open(new LCIMClientCallback() {
          @Override
          public void done(LCIMClient client, LCIMException e) {
            if (null != e) {
              System.out.println("failed to open receiver client");
              e.printStackTrace();
            } else {
              System.out.println(client.getClientId() + " logged in...");
            }
            receierOnlineLatch.countDown();
            tmpCounter.countDown();
          }
        });
        try {
          tmpCounter.await();
        } catch (Exception ex) {
          ex.printStackTrace();
        }

        try {
          Thread.sleep(10000);
        } catch (Exception ex) {
          ex.printStackTrace();
        }
        client.close(new LCIMClientCallback() {
          @Override
          public void done(LCIMClient client, LCIMException e) {
            if (null != e) {
              System.out.println("failed to close receiver client");
              e.printStackTrace();
            }
            tmpCounter.countDown();
          }
        });
      }
    };
    Thread t1 = new Thread(sendThread);
    t1.start();
    Thread t2 = new Thread(receiveThread);
    t2.start();

    t1.join();
    t2.join();
    int notifyCount = this.conversationEventHandler.getCount(0x0000FFFF);
    System.out.println("notifyCount=" + notifyCount);
    assertTrue(notifyCount > 2);
  }
}
