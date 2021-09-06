package cn.leancloud.im.v2;

import cn.leancloud.Configure;
import cn.leancloud.LCLogger;
import cn.leancloud.core.LeanCloud;
import cn.leancloud.im.LCIMOptions;
import cn.leancloud.im.v2.callback.*;
import cn.leancloud.json.JSONObject;
import cn.leancloud.session.LCConnectionManager;
import cn.leancloud.utils.StringUtil;
import junit.framework.TestCase;

import java.util.*;
import java.util.concurrent.CountDownLatch;

public class LCConversationTests extends TestCase {
    private String targetConversationId = null;
    CountDownLatch firstStage = null;
    CountDownLatch secondStage = null;
    CountDownLatch endStage = null;
    boolean testSucceed = false;

    public LCConversationTests(String name) {
        super(name);
        LeanCloud.setLogLevel(LCLogger.Level.DEBUG);
        Configure.initialize();
        LCIMOptions.getGlobalOptions().setTimeoutInSecs(30);
        LCIMMessageManager.setConversationEventHandler(new LCIMConversationEventHandler() {
            @Override
            public void onMemberLeft(LCIMClient client, LCIMConversation conversation, List<String> members, String kickedBy) {

            }

            @Override
            public void onMemberJoined(LCIMClient client, LCIMConversation conversation, List<String> members, String invitedBy) {

            }

            @Override
            public void onKicked(LCIMClient client, LCIMConversation conversation, String kickedBy) {

            }

            @Override
            public void onInvited(LCIMClient client, LCIMConversation conversation, String operator) {

            }

            @Override
            public void onInfoChanged(LCIMClient client, LCIMConversation conversation, JSONObject attr,
                                      String operator) {
                super.onInfoChanged(client, conversation, attr, operator);
            }
        });

        LCIMMessageManager.registerDefaultMessageHandler(new LCIMMessageHandler());
    }

    @Override
    protected void setUp() throws Exception {
        LCConnectionManager manager = LCConnectionManager.getInstance();
        manager.autoConnection();
        Thread.sleep(2000);
        firstStage = new CountDownLatch(1);
        secondStage = new CountDownLatch(1);
        endStage = new CountDownLatch(1);
        testSucceed = false;
    }

    public void testConversationMemberWithMutedBlockedStatus() throws Exception {
        final String clientId = "TestUserA";
        final String secondMember = StringUtil.getRandomString(8);
        final String thirdMember = StringUtil.getRandomString(8);
        final String customAttr = StringUtil.getRandomString(16);
        final Date now = new Date();
        LCIMClient currentClient = LCIMClient.getInstance(clientId);
        final CountDownLatch latch = new CountDownLatch(1);
        currentClient.open(new LCIMClientCallback() {
            @Override
            public void done(LCIMClient client, LCIMException e) {
                if (null != e) {
                    System.out.println("❌　failed to open client:" + clientId);
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
                members.add(secondMember);

                client.createConversation(members, "testMembers-" + now, attr, new LCIMConversationCreatedCallback() {
                    @Override
                    public void done(final LCIMConversation conversation, LCIMException e) {
                        if (null != e) {
                            System.out.println("❌　" + clientId + " failed to create conversation: testAttributesWithSingleClient");
                            e.printStackTrace();
                            latch.countDown();
                            return;
                        }
                        System.out.println("☑️☑️ " + clientId + " succeed to create conversation, data=" + conversation.toJSONString());
                        Map<String, Object> checkpoint = new HashMap<>();
                        checkpoint.put("name", "testMembers-" + now);
                        checkpoint.put("memberSize", 3);
                        checkpoint.put("attr.attr1", customAttr);
                        checkpoint.put("attr.attr2", now);
                        boolean assertResult = InteractiveTest.verifyConversationWithExpect(conversation, checkpoint);
                        if (!assertResult) {
                            System.out.println("❌　Site:" + clientId + " conversation doesn't match expected.");
                        } else {
                            System.out.println("checkpoint all passed.");
                        }

                        System.out.println("☑️☑️ " + clientId + " continue to mute conversation...");

                        conversation.mute(new LCIMConversationCallback() {
                            @Override
                            public void done(LCIMException e) {
                                if (null != e) {
                                    System.out.println("❌　failed to mute conversation.");
                                    e.printStackTrace();
                                    latch.countDown();
                                    return;
                                }
                                System.out.println("☑️☑️☑️ " + clientId + " already mute conversation.");
                                conversation.muteMembers(Arrays.asList(secondMember), new LCIMOperationPartiallySucceededCallback() {
                                    @Override
                                    public void done(LCIMException e, List<String> successfulClientIds, List<LCIMOperationFailure> failures) {
                                        if (null != e) {
                                            System.out.println("❌　failed to mute member within conversation.");
                                            e.printStackTrace();
                                            latch.countDown();
                                            return;
                                        }
                                        System.out.println("☑️☑️☑️️☑️ " + clientId + " already mute member within conversation.");
                                        conversation.blockMembers(Arrays.asList(thirdMember), new LCIMOperationPartiallySucceededCallback() {
                                            @Override
                                            public void done(LCIMException e, List<String> successfulClientIds, List<LCIMOperationFailure> failures) {
                                                if (null != e) {
                                                    System.out.println("❌　failed to block member within conversationinfo.");
                                                    e.printStackTrace();
                                                    latch.countDown();
                                                    return;
                                                }
                                                System.out.println("️☑️☑️☑️☑️☑️ " + clientId + " already block member within conversation.");
                                                conversation.fetchInfoInBackground(new LCIMConversationCallback() {
                                                    @Override
                                                    public void done(LCIMException e) {
                                                        if (null != e) {
                                                            System.out.println("❌　failed to fetch conversationinfo.");
                                                            e.printStackTrace();
                                                            latch.countDown();
                                                            return;
                                                        }
                                                        System.out.println("️☑️☑️☑️☑️☑️☑️ " + clientId + " already fetch conversation info.");
                                                        Object muteMembers = conversation.get("mu");
                                                        System.out.println("mu: " + muteMembers.toString() + "，" + conversation.get("muted"));
                                                        if (null == muteMembers) {
                                                            System.out.println("❌　mu result is wrong.");
                                                            latch.countDown();
                                                            return;
                                                        }
                                                        conversation.queryBlockedMembers(0, 10, new LCIMConversationSimpleResultCallback() {
                                                            @Override
                                                            public void done(List<String> memberIdList, LCIMException e) {
                                                                if (null != e) {
                                                                    System.out.println("❌　failed to query blocked members.");
                                                                    e.printStackTrace();
                                                                    latch.countDown();
                                                                    return;
                                                                }
                                                                if (null == memberIdList || memberIdList.size() < 1) {
                                                                    System.out.println("❌　blocked members result is wrong.");
                                                                    latch.countDown();
                                                                    return;
                                                                }
                                                                conversation.queryMutedMembers(0, 10, new LCIMConversationSimpleResultCallback() {
                                                                    @Override
                                                                    public void done(List<String> memberIdList, LCIMException e) {
                                                                        if (null != e) {
                                                                            System.out.println("❌　failed to query muted members.");
                                                                            e.printStackTrace();
                                                                            latch.countDown();
                                                                            return;
                                                                        }
                                                                        if (null == memberIdList || memberIdList.size() < 1) {
                                                                            System.out.println("❌　muted members result is wrong.");
                                                                        } else {
                                                                            testSucceed = true;
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
}
