package org.app.picture;

import java.io.File;
import java.io.FileOutputStream;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.Bitmap.CompressFormat;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.SurfaceHolder.Callback;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity implements OnClickListener{
	private static final String TAG = "MainActivity";
    private SurfaceView surfaceView;
    private Camera camera;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
    	requestWindowFeature(Window.FEATURE_NO_TITLE);//没有标题
    	window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);// 设置全屏
    	window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//高亮
        setContentView(R.layout.main);
        
        surfaceView = (SurfaceView) this.findViewById(R.id.surfaceView);
        surfaceView.getHolder().setFixedSize(176, 144);	//设置分辨率
        /*下面设置Surface不维护自己的缓冲区，而是等待屏幕的渲染引擎将内容推送到用户面前*/
        surfaceView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceView.getHolder().addCallback(new SurfaceCallback());
        
        Button button = (Button)findViewById(R.id.button1);
        button.setOnClickListener(this);
    }
    
    @Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(camera!=null && event.getRepeatCount()==0){
			switch (keyCode) {
			case KeyEvent.KEYCODE_SEARCH://搜索按钮
				camera.autoFocus(null);//自动对焦
				return true;

			case KeyEvent.KEYCODE_CAMERA://拍照按钮
			case KeyEvent.KEYCODE_DPAD_CENTER://确认按钮
				camera.takePicture(null, null, new JPGCallback());
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
    
    private final class JPGCallback implements PictureCallback{
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {//jpg图片的数据
			try {
				Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
				File file = new File(Environment.getExternalStorageDirectory(), System.currentTimeMillis()+ ".jpg");
				FileOutputStream outStream = new FileOutputStream(file);
				bitmap.compress(CompressFormat.JPEG, 100, outStream);
				outStream.close();
				camera.startPreview();
			} catch (Exception e) {
				Log.e(TAG, e.toString());
			}
		}    	
    }

	private final class SurfaceCallback implements Callback{    	
    	private boolean preview;
		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		}
		
		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			try {
				WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
				Display display = wm.getDefaultDisplay();
				camera = Camera.open();//打开摄相头
				Camera.Parameters parameters = camera.getParameters();
				parameters.setPreviewSize(display.getWidth(), display.getHeight());//设置预览照片的大小
				parameters.setPreviewFrameRate(5);//每秒3帧
				parameters.setPictureFormat(PixelFormat.JPEG);//设置照片的输出格式
				parameters.set("jpeg-quality", 85);//照片质量
				parameters.setPictureSize(display.getWidth(), display.getHeight());//设置照片的大小
				camera.setParameters(parameters);			
				camera.setPreviewDisplay(surfaceView.getHolder());//通过SurfaceView显示取景画面
				camera.startPreview();//显示预览画面
				preview = true;
				
			} catch (Exception e) {
				Log.e(TAG, e.toString());
			}
		}
		
		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			if(camera!=null){
				if(preview) camera.stopPreview();
				camera.release();
			}
		}    	
    }

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button1:
			camera.takePicture(null, null, new JPGCallback());
			break;

		default:
			break;
		}
	}
}