package cn.leancloud.realtime_sample_app;

import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

import cn.leancloud.AVException;
import cn.leancloud.AVFile;
import cn.leancloud.AVObject;
import cn.leancloud.AVQuery;
import cn.leancloud.cache.PersistenceUtil;
import cn.leancloud.im.v2.AVIMClient;
import cn.leancloud.im.v2.AVIMClientEventHandler;
import cn.leancloud.im.v2.AVIMConversation;
import cn.leancloud.im.v2.AVIMConversationsQuery;
import cn.leancloud.im.v2.AVIMException;
import cn.leancloud.im.v2.callback.AVIMConversationCallback;
import cn.leancloud.im.v2.callback.AVIMConversationCreatedCallback;
import cn.leancloud.im.v2.callback.AVIMConversationQueryCallback;
import cn.leancloud.im.v2.messages.AVIMAudioMessage;
import cn.leancloud.livequery.AVLiveQuery;
import cn.leancloud.livequery.AVLiveQueryConnectionHandler;
import cn.leancloud.livequery.AVLiveQueryEventHandler;
import cn.leancloud.livequery.AVLiveQuerySubscribeCallback;
import cn.leancloud.push.PushService;
import cn.leancloud.utils.FileUtil;

public class MainActivity extends AppCompatActivity {

  private TextView mTextMessage;

  private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
      = new BottomNavigationView.OnNavigationItemSelectedListener() {

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
      AVIMClient currentClient = AVIMClient.getInstance(AVIMClient.getDefaultClient());
      switch (item.getItemId()) {
        case R.id.navigation_home:
          mTextMessage.setText(R.string.title_home);

          AVIMConversationsQuery query = currentClient.getConversationsQuery();
          query.setLimit(20);
          query.findInBackground(new AVIMConversationQueryCallback() {
            @Override
            public void done(List<AVIMConversation> conversations, AVIMException e) {
              if (e != null) {
                Log.e("tag", "conversations error ", e);
              } else {
                Log.e("tag", "conversations done " + conversations);
              }
            }
          });
          return true;
        case R.id.navigation_dashboard:
          mTextMessage.setText(R.string.title_dashboard);
          AVQuery productQuery = new AVQuery<AVObject>("Product");
          productQuery.whereExists("title");
          AVLiveQuery liveQuery = AVLiveQuery.initWithQuery(productQuery);
          AVLiveQuery.setConnectionHandler(new AVLiveQueryConnectionHandler() {
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
          liveQuery.setEventHandler(new AVLiveQueryEventHandler() {
            @Override
            public void done(AVLiveQuery.EventType eventType, AVObject avObject, List<String> updateKeyList) {
              super.done(eventType, avObject, updateKeyList);
            }
            @Override
            public void onObjectCreated(AVObject avObject) {
              System.out.println("object created: " + avObject);
            }
            @Override
            public void onObjectDeleted(String objectId) {
              System.out.println("object deleted: " + objectId);
            }
          });
          liveQuery.subscribeInBackground(new AVLiveQuerySubscribeCallback() {
            @Override
            public void done(AVException e) {
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
            AVIMAudioMessage audioMessage = new AVIMAudioMessage(MainActivity.this.getCacheDir().getAbsolutePath() + "/dYRQ8YfHavfile/c1fa842a72de9129f5b2b342cbeb3c9d");
            currentClient.createTemporaryConversation(Arrays.asList("abc", "def"), new AVIMConversationCreatedCallback() {
              @Override
              public void done(AVIMConversation conversation, AVIMException e) {
                if (e != null) {
                  Log.e("tag", "failed to create conversations. error ", e);
                } else {
                  conversation.sendMessage(audioMessage, new AVIMConversationCallback() {
                    @Override
                    public void done(AVIMException ex) {
                      if (null != ex) {
                        Log.e("tag", "failed to send Audio Message, cause: " + ex.getMessage());
                      } else {
                        Log.d("tag", "succeed to send audio message.");
                      }
                    }
                  });
                }
              }
            });
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
//              AVIMAudioMessage audioMessage = new AVIMAudioMessage(MainActivity.this.getCacheDir().getAbsolutePath() + "/dYRQ8YfHavfile/c1fa842a72de9129f5b2b342cbeb3c9d");
//
//              AVIMClient currentClient = AVIMClient.getInstance(AVIMClient.getDefaultClient());
//              currentClient.createTemporaryConversation(Arrays.asList("abc", "def"), new AVIMConversationCreatedCallback() {
//                @Override
//                public void done(AVIMConversation conversation, AVIMException e) {
//                  if (e != null) {
//                    Log.e("tag", "failed to create conversations. error ", e);
//                  } else {
//                    conversation.sendMessage(audioMessage, new AVIMConversationCallback() {
//                      @Override
//                      public void done(AVIMException ex) {
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
