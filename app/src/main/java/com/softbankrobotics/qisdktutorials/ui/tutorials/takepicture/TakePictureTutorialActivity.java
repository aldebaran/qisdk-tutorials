package com.softbankrobotics.qisdktutorials.ui.tutorials.takepicture;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.aldebaran.qi.Consumer;
import com.aldebaran.qi.Function;
import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.Qi;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.builder.TakePictureBuilder;
import com.aldebaran.qi.sdk.object.camera.TakePicture;
import com.aldebaran.qi.sdk.object.conversation.Say;
import com.aldebaran.qi.sdk.object.image.EncodedImage;
import com.aldebaran.qi.sdk.object.image.EncodedImageHandle;
import com.aldebaran.qi.sdk.object.image.TimestampedImageHandle;
import com.softbankrobotics.qisdktutorials.R;
import com.softbankrobotics.qisdktutorials.ui.conversation.ConversationItemType;
import com.softbankrobotics.qisdktutorials.ui.conversation.ConversationView;
import com.softbankrobotics.qisdktutorials.ui.tutorials.TutorialActivity;

import java.nio.ByteBuffer;

/**
 * The activity for the take picture tutorial.
 */
public class TakePictureTutorialActivity extends TutorialActivity implements RobotLifecycleCallbacks {

    private static final String TAG = "TakePictureActivity";

    private ConversationView conversationView;

    // The QiContext provided by the QiSDK.
    private QiContext qiContext;

    private ImageView pictureView;
    private ProgressBar progressBar;
    private Button takePicButton;
    private Bitmap pictureBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        conversationView = findViewById(R.id.conversationView);
        pictureView = findViewById(R.id.picture_view);
        progressBar = findViewById(R.id.progressBar);
        takePicButton = findViewById(R.id.take_pic);

        takePicButton.setEnabled(false);
        takePicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePic();
            }
        });

        // Register the RobotLifecycleCallbacks to this Activity.
        QiSDK.register(this, this);
    }


    @Override
    protected void onDestroy() {
        // Unregister the RobotLifecycleCallbacks for this Activity.
        QiSDK.unregister(this, this);
        super.onDestroy();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_take_picture_tutorial;
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        // Store the provided QiContext.
        this.qiContext = qiContext;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                takePicButton.setEnabled(true);
            }
        });
        String textToSay = "I can take pictures. Press the button to try!";
        displayLine(textToSay, ConversationItemType.ROBOT_OUTPUT);

        Say say = SayBuilder.with(qiContext)
                .withText(textToSay)
                .build();

        say.run();
    }

    @Override
    public void onRobotFocusLost() {
        Log.i(TAG, "Focus lost.");
        // Remove the QiContext.
        this.qiContext = null;

    }

    @Override
    public void onRobotFocusRefused(String reason) {
        Log.i(TAG, "onRobotFocusRefused: " + reason);
    }

    private void displayLine(final String text, final ConversationItemType type) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                conversationView.addLine(text, type);
            }
        });
    }


    private void takePic() {
        if (qiContext == null) {
            return;
        }

        if (pictureBitmap != null) {
            pictureBitmap.recycle();
            pictureBitmap = null;
            pictureView.setImageBitmap(null);
        }

        Log.i(TAG, "build take picture");
        // Build the action.
        Future<TakePicture> takePictureFuture = TakePictureBuilder.with(qiContext).buildAsync();
        // Take picture
        takePictureFuture.andThenCompose(Qi.onUiThread(new Function<TakePicture, Future<TimestampedImageHandle>>() {
            @Override
            public Future<TimestampedImageHandle> execute(TakePicture takePicture) {
                Log.i(TAG, "take picture launched!");
                progressBar.setVisibility(View.VISIBLE);
                takePicButton.setEnabled(false);
                return takePicture.async().run();
            }
        })).andThenConsume(new Consumer<TimestampedImageHandle>() {
            @Override
            public void consume(TimestampedImageHandle timestampedImageHandle) throws Throwable {
                //Consume take picture action when it's ready
                Log.i(TAG, "Picture taken");
                // get picture
                EncodedImageHandle encodedImageHandle = timestampedImageHandle.getImage();

                EncodedImage encodedImage = encodedImageHandle.getValue();
                Log.i(TAG, "PICTURE RECEIVED!");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        takePicButton.setEnabled(true);
                    }
                });

                ByteBuffer buffer = encodedImage.getData();
                buffer.rewind();
                final int pictureBufferSize = buffer.remaining();
                final byte[] pictureArray = new byte[pictureBufferSize];
                buffer.get(pictureArray);

                Log.i(TAG, "PICTURE RECEIVED! (" + pictureBufferSize + " Bytes)");
                pictureBitmap = BitmapFactory.decodeByteArray(pictureArray, 0, pictureBufferSize);
                // display picture
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pictureView.setImageBitmap(pictureBitmap);
                    }
                });

            }
        });

    }

}

