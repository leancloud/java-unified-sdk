package cn.leancloud;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class DeletedUserTest extends TestCase {
    private static final String USER_PREFIX = "DeletedUserTest";
    private List<String> testObjectIds = null;
    private boolean operationSucceed = false;
    private CountDownLatch latch = null;

    public DeletedUserTest(String name) {
        super(name);
        Configure.initializeRuntime();
    }

    public static Test suite() {
        return new TestSuite(DeletedUserTest.class);
    }

    @Override
    protected void setUp() throws Exception {
        testObjectIds = new ArrayList<>(10);
        String username = null;
        String userEmail = null;
        for (int i = 0; i < 10; i++) {
            username = USER_PREFIX + i;
            userEmail = username + "@abc.com";
            LCUser result = UserFollowshipTest.prepareUser(username, userEmail, true);
            testObjectIds.add(result.getObjectId());
        }
        latch = new CountDownLatch(1);
        operationSucceed = false;
    }

    @Override
    protected void tearDown() throws Exception {
        LCUser.logOut();
    }

    private void prepareFollowship() throws Exception {
        LCUser current = LCUser.logIn(USER_PREFIX+9, UserFollowshipTest.DEFAULT_PASSWD).blockingFirst();
        for(int i = 0;i < 9; i++) {
            current.followInBackground(testObjectIds.get(i)).blockingFirst();
        }
    }

    public void testFollowList() throws Exception {
        prepareFollowship();

        LCUser current = LCUser.logIn(USER_PREFIX+9, UserFollowshipTest.DEFAULT_PASSWD).blockingFirst();
        current.followeeQuery().findInBackground().subscribe(new Observer<List<LCObject>>() {
            @Override
            public void onSubscribe(@NotNull Disposable disposable) {

            }

            @Override
            public void onNext(@NotNull List<LCObject> lcObjects) {
                for (LCObject obj: lcObjects) {
                    System.out.println(obj);
                }
                operationSucceed = true;
                latch.countDown();
            }

            @Override
            public void onError(@NotNull Throwable throwable) {
                latch.countDown();
            }

            @Override
            public void onComplete() {

            }
        });
        latch.await();
        assertTrue(operationSucceed);
    }

    private void prepareFriendship() throws Exception {
        LCUser current = LCUser.logIn(USER_PREFIX+9, UserFollowshipTest.DEFAULT_PASSWD).blockingFirst();
        List<LCFriendshipRequest> requests = new ArrayList<>(9);
        for (int i = 0; i < 9; i++) {
            LCUser target = LCUser.createWithoutData(LCUser.class, testObjectIds.get(i));
            LCFriendshipRequest request = current.applyFriendshipInBackground(target, null).blockingFirst();
            requests.add(request);
        }
        for (int i = 0; i < 9; i++) {
            LCUser tmp = LCUser.logIn(USER_PREFIX+i, UserFollowshipTest.DEFAULT_PASSWD).blockingFirst();
            tmp.acceptFriendshipRequest(requests.get(i), null).blockingFirst();
        }
    }
    public void testFriendList() throws Exception {
        prepareFriendship();
        LCUser current = LCUser.logIn(USER_PREFIX+9, UserFollowshipTest.DEFAULT_PASSWD).blockingFirst();
        List<LCFriendship> friendships = current.queryFriendship().blockingFirst();
        for (LCFriendship fs: friendships) {
            System.out.println(fs);
        }
    }
}
