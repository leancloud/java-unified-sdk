package cn.leancloud.realtime_sample_app;

import android.os.Bundle;

import androidx.annotation.NonNull;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import cn.leancloud.LCException;
import cn.leancloud.LCFile;
import cn.leancloud.LCObject;
import cn.leancloud.LCQuery;
import cn.leancloud.cache.PersistenceUtil;
import cn.leancloud.im.v2.LCIMClient;
import cn.leancloud.im.v2.LCIMClientEventHandler;
import cn.leancloud.im.v2.LCIMConversation;
import cn.leancloud.im.v2.LCIMConversationsQuery;
import cn.leancloud.im.v2.LCIMException;
import cn.leancloud.im.v2.callback.LCIMConversationCallback;
import cn.leancloud.im.v2.callback.LCIMConversationCreatedCallback;
import cn.leancloud.im.v2.callback.LCIMConversationIterableResult;
import cn.leancloud.im.v2.callback.LCIMConversationIterableResultCallback;
import cn.leancloud.im.v2.callback.LCIMConversationMemberCountCallback;
import cn.leancloud.im.v2.callback.LCIMConversationMemberQueryCallback;
import cn.leancloud.im.v2.callback.LCIMConversationQueryCallback;
import cn.leancloud.im.v2.callback.LCIMConversationSimpleResultCallback;
import cn.leancloud.im.v2.callback.LCIMOperationFailure;
import cn.leancloud.im.v2.callback.LCIMOperationPartiallySucceededCallback;
import cn.leancloud.im.v2.conversation.LCIMConversationMemberInfo;
import cn.leancloud.im.v2.conversation.ConversationMemberRole;
import cn.leancloud.im.v2.messages.LCIMAudioMessage;
import cn.leancloud.livequery.LCLiveQuery;
import cn.leancloud.livequery.LCLiveQueryConnectionHandler;
import cn.leancloud.livequery.LCLiveQueryEventHandler;
import cn.leancloud.livequery.LCLiveQuerySubscribeCallback;
import cn.leancloud.push.PushService;
import cn.leancloud.utils.FileUtil;

public class MainActivity extends AppCompatActivity {

  private TextView mTextMessage;

  private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
      = new BottomNavigationView.OnNavigationItemSelectedListener() {

    private void testConvMemberOperations(final LCIMConversation conv) {
      conv.getAllMemberInfo(0, 100, new LCIMConversationMemberQueryCallback() {
        @Override
        public void done(List<LCIMConversationMemberInfo> memberInfoList, LCIMException e) {
          if (null != e) {
            Log.e("member_query", "memberInfo query error ", e);
          } else {
            Log.d("member_query", "memberInfo query result " + memberInfoList);
            conv.blockMembers(Arrays.asList("Yoda"), new LCIMOperationPartiallySucceededCallback() {
              @Override
              public void done(LCIMException e, List<String> successfulClientIds, List<LCIMOperationFailure> failures) {
                if (null != e) {
                  Log.e("member_block", "block member error ", e);
                } else {
                  Log.d("member_block", successfulClientIds.toString());
                  conv.queryBlockedMembers(100, null, new LCIMConversationIterableResultCallback() {
                    @Override
                    public void done(LCIMConversationIterableResult iterableResult, LCIMException e) {
                      if (null != e) {
                        Log.e("blocked_query", "block member query error ", e);
                      } else {
                        Log.d("blocked_query(items) ", iterableResult.getMembers().toString());
                        Log.d("blocked_query(hasNext) ", String.valueOf(iterableResult.hasNext()));
                      }
                      conv.unblockMembers(Arrays.asList("Yoda"), new LCIMOperationPartiallySucceededCallback() {
                        @Override
                        public void done(LCIMException e, List<String> successfulClientIds, List<LCIMOperationFailure> failures) {
                          if (null != e) {
                            Log.e("member_unblock", "unblock member error ", e);
                          } else {
                            Log.d("member_unblock", successfulClientIds.toString());
                            conv.muteMembers(Arrays.asList("Luke"), new LCIMOperationPartiallySucceededCallback() {
                              @Override
                              public void done(LCIMException e, List<String> successfulClientIds, List<LCIMOperationFailure> failures) {
                                if (null != e) {
                                  Log.e("member_mute", "muted member error ", e);
                                } else {
                                  Log.d("member_mute", successfulClientIds.toString());
                                  conv.queryMutedMembers(0, 100, new LCIMConversationSimpleResultCallback() {
                                    @Override
                                    public void done(List<String> memberIdList, LCIMException e) {
                                      if (null != e) {
                                        Log.e("muted_query", "muted member query error ", e);
                                      } else {
                                        Log.d("muted_query", memberIdList.toString());
                                        conv.unmuteMembers(Arrays.asList("Luke"), new LCIMOperationPartiallySucceededCallback() {
                                          @Override
                                          public void done(LCIMException e, List<String> successfulClientIds, List<LCIMOperationFailure> failures) {
                                            if (null != e) {
                                              Log.e("member_unmute", "unmute member error ", e);
                                            } else {
                                              Log.d("member_unmute", successfulClientIds.toString());
                                            }
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
                    }
                  });
                }
              }
            });
          }
        }
      });
    }
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
      LCIMClient currentClient = LCIMClient.getInstance(LCIMClient.getDefaultClient());
      switch (item.getItemId()) {
        case R.id.navigation_home:
          mTextMessage.setText(R.string.title_home);

          LCIMConversationsQuery query = currentClient.getConversationsQuery();
          query.setLimit(20);
          query.findInBackground(new LCIMConversationQueryCallback() {
            @Override
            public void done(List<LCIMConversation> conversations, LCIMException e) {
              if (e != null) {
                Log.e("tag", "conversations query error ", e);
              } else {
                Log.e("tag", "conversations query done " + conversations);
                if (conversations.size() > 0) {
                  final LCIMConversation conv = conversations.get(0);
                  conv.getMemberCount(new LCIMConversationMemberCountCallback() {
                    @Override
                    public void done(Integer memberCount, LCIMException e) {
                      if (null != e) {
                        Log.e("tag", "conversations member count error ", e);
                        e.printStackTrace();
                      } else {
                        Log.d("tag", "conversation member count:" + memberCount);
                        testConvMemberOperations(conv);
                      }
                    }
                  });
                } else {
                  currentClient.createConversation(Arrays.asList("Yoda", "Obiwan", "Luke"), "YodaUniqueTest2", null, false, true, new LCIMConversationCreatedCallback() {
                    @Override
                    public void done(LCIMConversation conversation, LCIMException e) {
                      if (null != e) {
                        Log.e("tag", "conversations create error ", e);
                        e.printStackTrace();
                      } else {
                        conversation.getMemberCount(new LCIMConversationMemberCountCallback() {
                          @Override
                          public void done(Integer memberCount, LCIMException e) {
                            if (null != e) {
                              Log.e("tag", "conversations member count error ", e);
                              e.printStackTrace();
                            } else {
                              Log.e("tag", "conversation member count:" + memberCount);
                              testConvMemberOperations(conversation);
                            }
                          }
                        });
                      }
                    }
                  });
                }
              }
            }
          });
          return true;
        case R.id.navigation_dashboard:
          mTextMessage.setText(R.string.title_dashboard);
          LCQuery productQuery = new LCQuery<LCObject>("Product");
          productQuery.whereExists("title");
          LCLiveQuery liveQuery = LCLiveQuery.initWithQuery(productQuery);
          LCLiveQuery.setConnectionHandler(new LCLiveQueryConnectionHandler() {
            @Override
            public void onConnectionOpen() {
              System.out.println("============ LiveQuery Connection opened ============");
            }

            @Override
            public void onConnectionClose() {
              System.out.println("============ LiveQuery Connection closed ============");
            }

            @Override
            public void onConnectionError(int code, String reason) {
              System.out.println("============ LiveQuery Connection error. code:" + code
                  + ", reason:" + reason + " ============");
            }
          });
          liveQuery.setEventHandler(new LCLiveQueryEventHandler() {
            @Override
            public void done(LCLiveQuery.EventType eventType, LCObject avObject, List<String> updateKeyList) {
              super.done(eventType, avObject, updateKeyList);
            }

            @Override
            public void onObjectCreated(LCObject avObject) {
              System.out.println("object created: " + avObject);
            }

            @Override
            public void onObjectDeleted(String objectId) {
              System.out.println("object deleted: " + objectId);
            }
          });
          liveQuery.subscribeInBackground(new LCLiveQuerySubscribeCallback() {
            @Override
            public void done(LCException e) {
              if (null != e) {
                System.out.println("failed to subscribe livequery.");
                e.printStackTrace();
              } else {
                System.out.println("succeed to subscribe livequery.");
              }
            }
          });
          return true;
        case R.id.navigation_notifications:
          mTextMessage.setText(R.string.title_notifications);
          try {
            List<String> members = Arrays.asList("testUser2", "testUser3", "testUser4");
            LCIMConversationsQuery convQuery1 = currentClient.getConversationsQuery();
            convQuery1.setQueryPolicy(LCQuery.CachePolicy.NETWORK_ONLY);
            convQuery1.containsMembers(members).findInBackground(new LCIMConversationQueryCallback() {
              @Override
              public void done(List<LCIMConversation> conversations, LCIMException e1) {
                if (null != e1) {
                  e1.printStackTrace();
                  return;
                }
                for (LCIMConversation conv: conversations) {
                  System.out.println(conv.getCreatedAt());
                  System.out.println(conv.toJSONString());
                }
                LCIMConversationsQuery convQuery2 = currentClient.getConversationsQuery();
                convQuery2.setQueryPolicy(LCQuery.CachePolicy.CACHE_ONLY);
                convQuery2.containsMembers(members).findInBackground(new LCIMConversationQueryCallback() {
                  @Override
                  public void done(List<LCIMConversation> conversations, LCIMException e2) {
                    if (null != e2) {
                      e2.printStackTrace();
                      return;
                    }
                    for (LCIMConversation conv: conversations) {
                      System.out.println(conv.getCreatedAt());
                      System.out.println(conv.toJSONString());
                    }
                  }
                });
              }
            });

//            currentClient.createConversation(members, "UnitTestConversation", null, false, true, new LCIMConversationCreatedCallback() {
//              @Override
//              public void done(final LCIMConversation conversation, LCIMException e) {
//                if (null != e) {
//                  System.out.println("failed to create conversation. cause: " + e.getMessage());
//                } else {
//                  System.out.println("succeed to create conversation, continue to update member role...");
//                  conversation.updateMemberRole("testUser2", ConversationMemberRole.MANAGER, new LCIMConversationCallback() {
//                    @Override
//                    public void done(LCIMException e1) {
//                      if (null != e1) {
//                        System.out.println("failed to promote testUser2. cause: " + e1.getMessage());
//                      } else {
//                        System.out.println("succeed to promote testUser2.");
//                        conversation.updateMemberRole("testUser3", ConversationMemberRole.MEMBER, new LCIMConversationCallback() {
//                          @Override
//                          public void done(LCIMException e2) {
//                            if (null != e2) {
//                              System.out.println("failed to promote testUser3. cause: " + e2.getMessage());
//                            } else {
//                              System.out.println("succeed to promote testUser3.");
//                              conversation.getAllMemberInfo(0, 10, new LCIMConversationMemberQueryCallback() {
//                                @Override
//                                public void done(List<LCIMConversationMemberInfo> memberInfoList, LCIMException e3) {
//                                  if (null == e3) {
//                                    for (LCIMConversationMemberInfo info: memberInfoList) {
//                                      System.out.println("memberInfo: " + info.toString());
//                                    }
//                                  } else {
//                                    System.out.println("failed to query memberInfo. cause: " + e3.getMessage());
//                                    e3.printStackTrace();
//                                  }
//                                }
//                              });
//                            }
//                          }
//                        });
//                      }
//                    }
//                  });
//                }
//              }
//            });


//            AVFile file = new AVFile("apple.acc", "https://some.website.com/apple.acc", new HashMap<>());
//            LCIMAudioMessage m = new LCIMAudioMessage(file);
//            m.setText("来自苹果发布会现场的录音");
//            currentClient.createTemporaryConversation(Arrays.asList("abc", "def"), new LCIMConversationCreatedCallback() {
//              @Override
//              public void done(LCIMConversation conversation, LCIMException e) {
//                if (e != null) {
//                  Log.e("tag", "failed to create conversations. error ", e);
//                } else {
//                  conversation.sendMessage(m, new LCIMConversationCallback() {
//                    @Override
//                    public void done(LCIMException ex) {
//                      if (null != ex) {
//                        ex.printStackTrace();
//                        Log.e("tag", "failed to send Audio Message, cause: " + ex.getMessage());
//                      } else {
//                        Log.d("tag", "succeed to send audio message.");
//                      }
//                    }
//                  });
//                }
//              }
//            });
          } catch (Exception ex) {
            ex.printStackTrace();
          }
//          AVFile avFile = new AVFile("audioTestFile", "http://youpeng.nineck.com/GOCuNRpeldzQOsxZF7DGCoiryFHdgvyjLsR0j2Ee.wav");
//          new Thread(new Runnable() {
//            @Override
//            public void run() {
//              byte[] audioData = avFile.getData();
//              Log.d("tag", "succeed to download audio data from network.");
//
//              AVFile audioFile = new AVFile("IM Audio Message File", audioData);
//
//              LCIMAudioMessage audioMessage = new LCIMAudioMessage(MainActivity.this.getCacheDir().getAbsolutePath() + "/dYRQ8YfHavfile/c1fa842a72de9129f5b2b342cbeb3c9d");
//
//              LCIMClient currentClient = LCIMClient.getInstance(LCIMClient.getDefaultClient());
//              currentClient.createTemporaryConversation(Arrays.asList("abc", "def"), new LCIMConversationCreatedCallback() {
//                @Override
//                public void done(LCIMConversation conversation, LCIMException e) {
//                  if (e != null) {
//                    Log.e("tag", "failed to create conversations. error ", e);
//                  } else {
//                    conversation.sendMessage(audioMessage, new LCIMConversationCallback() {
//                      @Override
//                      public void done(LCIMException ex) {
//                        if (null != ex) {
//                          Log.e("tag", "failed to send Audio Message, cause: " + ex.getMessage());
//                        } else {
//                          Log.d("tag", "succeed to send audio message.");
//                        }
//                      }
//                    });
//                  }
//                }
//              });
//            }
//          }).start();
          return true;
      }
      return false;
    }
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mTextMessage = (TextView) findViewById(R.id.message);
    BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
    navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

  }

  @Override
  protected void onStart() {
    super.onStart();
    System.out.println("MainActivity onStart()....");
    PushService.setDefaultPushCallback(this, MainActivity.class);
  }
}
