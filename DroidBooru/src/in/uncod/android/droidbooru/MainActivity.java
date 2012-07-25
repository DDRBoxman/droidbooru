package in.uncod.android.droidbooru;

import in.uncod.nativ.AbstractNetworkCallbacks;
import in.uncod.nativ.HttpClientNetwork;
import in.uncod.nativ.INetworkHandler;
import in.uncod.nativ.Image;
import in.uncod.nativ.KeyPredicate;
import in.uncod.nativ.ORMDatastore;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.MimeTypeMap;
import android.webkit.WebView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private final class AuthCallback extends AbstractNetworkCallbacks {
        private boolean mError;

        @Override
        public void onError(INetworkHandler handler, int errorCode, String message) {
            mError = true;

            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(MainActivity.this, "Login failed", Toast.LENGTH_LONG).show();
                }
            });
        }

        @Override
        public void finished(final String extras) {
            if (mError) {
                mConnectButton.setEnabled(true);
            }
            else {
                mDatastore.externalQueryImage(KeyPredicate.defaultPredicate().orderBy("uploadedDate", true),
                        null, new AbstractNetworkCallbacks() {
                            @Override
                            public void onReceivedImage(ORMDatastore ds, String queryName,
                                    final Image[] images) {
                                if (images.length > 0) {
                                    runOnUiThread(new Runnable() {
                                        public void run() {
                                            mConnectButton.setVisibility(View.GONE);
                                            mControls.setVisibility(View.VISIBLE);
                                            mImageView.setVisibility(View.VISIBLE);

                                            mImages = Arrays.asList(images);

                                            displayImage(0);
                                        }
                                    });
                                }
                                else {
                                    runOnUiThread(new Runnable() {
                                        public void run() {
                                            Toast.makeText(MainActivity.this, "No images",
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }
                            }
                        });
            }
        }
    }

    private List<Image> mImages = null;
    private ORMDatastore mDatastore;
    private View mConnectButton;
    private WebView mImageView;
    private File mDataDirectory;
    private View mNextButton;
    private int mImageIndex = 0;
    private View mControls;
    private View mPreviousButton;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDataDirectory = getFilesDir();

        mControls = findViewById(R.id.controls);

        mConnectButton = findViewById(R.id.button_connect);
        mConnectButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mConnectButton.setEnabled(false);

                mDatastore = ORMDatastore.create(new File(mDataDirectory, "droidbooru.db").getAbsolutePath());
                mDatastore.setDownloadPathPrefix(mDataDirectory.getAbsolutePath());
                mDatastore.setNetworkHandler(new HttpClientNetwork("http://img.uncod.in/v2/api"));
                mDatastore.authenticate("test", "test", new AuthCallback());
            }
        });

        mPreviousButton = findViewById(R.id.button_previous);
        mPreviousButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mImageIndex--;

                if (mImageIndex < 0)
                    mImageIndex = 0;

                displayImage(mImageIndex);
            }
        });

        mNextButton = findViewById(R.id.button_next);
        mNextButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mImageIndex++;

                if (mImageIndex > mImages.size() - 1)
                    mImageIndex = mImages.size() - 1;

                displayImage(mImageIndex);
            }
        });

        mImageView = (WebView) findViewById(R.id.image);
    }

    private void displayImage(int index) {
        Image image = mImages.get(index);

        String extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(image.getMime());

        String url = "http://img.uncod.in/img/" + image.getFilehash() + "." + extension;

        mImageView.loadUrl(url);
    }
}