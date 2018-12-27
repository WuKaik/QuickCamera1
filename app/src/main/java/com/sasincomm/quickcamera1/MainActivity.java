package com.sasincomm.quickcamera1;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 添加相机相关权限
 *通过Camera.open(int)获得一个相机实例
 *利用camera.getParameters()得到相机实例的默认设置Camera.Parameters
 *如果需要的话，修改Camera.Parameters并调用camera.setParameters(Camera.Parameters)来修改相机设置
 *调用camera.setDisplayOrientation(int)来设置正确的预览方向
 *调用camera.setPreviewDisplay(SurfaceHolder)来设置预览，如果没有这一步，相机是无法开始预览的
 *调用camera.startPreview()来开启预览，对于拍照，这一步是必须的
 *在需要的时候调用camera.takePicture(Camera.ShutterCallback, Camera.PictureCallback, Camera.PictureCallback, Camera.PictureCallback)来拍摄照片
 *拍摄照片后，相机会停止预览，如果需要再次拍摄多张照片，需要再次调用camera.startPreview()来重新开始预览
 *调用camera.stopPreview()来停止预览
 *一定要在onPause()的时候调用camera.release()来释放camera，在onResume中重新开始camera
 */
public class MainActivity extends AppCompatActivity {
    public static final String TAG="MainActivity";

    private SurfaceView surfaceView;
    private Button pictureBtn;

    private Camera mCamera;
    private SurfaceHolder mHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        surfaceView=findViewById(R.id.surfaceView);
        pictureBtn=findViewById(R.id.btn_picture);
        mCamera=getCamera();
        mHolder=surfaceView.getHolder();
        mHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                //创建
                if(mCamera!=null)
                {
                    startPreview(mCamera,holder);
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                //改变
                if(mCamera!=null)
                {
                    mCamera.stopPreview();
                    startPreview(mCamera,holder);
                }
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                //成功
                releaseCamera();
            }
        });
        //拍照
        pictureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mCamera!=null)
                {
                    /*参数的一些设置*/
                    Camera.Parameters parameters = mCamera.getParameters();
                    /*格式*/
                    parameters.setPictureFormat(ImageFormat.JPEG);
                    /*大小*/
                    parameters.setPreviewSize(800, 400);
                    /*对焦,这里设置的是自动对焦*/
                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                    mCamera.autoFocus(new Camera.AutoFocusCallback() {
                        @Override
                        public void onAutoFocus(boolean success, Camera camera) {
                            Log.d(TAG, "onAutoFocus: success=="+success);
                            if(success)
                            {
                                mCamera.takePicture(null, null, new Camera.PictureCallback() {
                                    @Override
                                    public void onPictureTaken(byte[] data, Camera camera) {
                                        Log.d(TAG, "onPictureTaken: 接收到拍照后的数据");
                                        /*data存储了整个图片的数据这里应该保存到指定的路径中去*/
                                        File tempFile = new File(Environment.getExternalStorageDirectory().getPath() + "/image.png");
                                        FileOutputStream fos = null;
                                        try {
                                            fos = new FileOutputStream(tempFile);
                                            fos.write(data);
                                            Toast.makeText(MainActivity.this,"拍照成功",Toast.LENGTH_SHORT).show();
                                        } catch (FileNotFoundException e) {
                                            e.printStackTrace();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        } finally {
                                            if (fos != null) {
                                                try {
                                                    fos.close();
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            //重新开始预览
                                            startPreview(mCamera,mHolder);
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

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume: ");
        if (mCamera == null) {
            mCamera = getCamera();
            if (mHolder != null) {
                startPreview(mCamera, mHolder);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
    }

    /**
     * 获取摄像头
     * @return
     */
    private Camera getCamera()
    {
        if(Camera.getNumberOfCameras()>0)
        {
            return Camera.open(0);
        }
        return null;
    }

    /**
     * 释放摄像头资源
     */
    private void releaseCamera()
    {
        if(mCamera!=null)
        {
            mCamera.setPreviewCallback(null);/*这个是预览的回调,里面会返回一个Byte[]和相应的Camera*/
            mCamera.stopPreview();/*取消预览功能*/
            mCamera.release();
            mCamera = null;
        }
    }

    /**
     * 开启预览
     * @param camera
     * @param holder
     */
    private void startPreview(Camera camera, SurfaceHolder holder)
    {
        Log.e(TAG, "setStartPreview: ");
        try {
            camera.setPreviewDisplay(holder);
            /*系统默认camera是横屏的*/
            /*设置角度 TODO 这里很重要的*/
            //camera.setDisplayOrientation(90);
            camera.setPreviewCallback(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {
                    Log.d(TAG, "onPreviewFrame: data size:"+data.length);
                }
            });
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
