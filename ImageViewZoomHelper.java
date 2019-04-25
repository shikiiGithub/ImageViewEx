package shikii;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;

import android.util.FloatMath;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

public class ImageViewZoomHelper   {
	public  ImageView iv ;
	Matrix originalMaxix;
    Bitmap bmp_Main ;
	private int nIndex_Image;

	public ImageViewZoomHelper(ImageView iv) {
		iv.setScaleType( ScaleType .MATRIX);
 		iv.setOnTouchListener(new TouchListener());
        this.iv = iv ;



	}
    public String Source = null ;
	public void Recover()
	{
		iv.getImageMatrix().reset();
		iv.setImageMatrix(originalMaxix);
		SetImageEx(Source);
	}
	public void SetImageEx(String src   )
	{
		try {
			 int nWidth = iv.getWidth() ;
			if(bmp_Main != null)
			bmp_Main.recycle();
			Source = src;
			Bitmap tempBMP = BitmapFactory.decodeFile(src);
			int width = tempBMP.getWidth();
			int height = tempBMP.getHeight();
			float ratio = height * 1.0f / width;
			int ndisplayHeight = (int) (nWidth * ratio);

			int dy = (int)(iv.getHeight()/2.0f- height/2.0f) ;
			originalMaxix.reset();
			originalMaxix.postTranslate(0,dy/4) ;
			bmp_Main = Bitmap.createScaledBitmap(tempBMP, nWidth, ndisplayHeight, true);
			iv.setImageBitmap(bmp_Main);
			iv.setImageMatrix(originalMaxix);
			 tempBMP.recycle();
		}
		catch (Exception e)
		{

		}

	}
    private final class TouchListener implements OnTouchListener{ 
    	 private PointF startPoint= new PointF();//PointF(浮点对) 
    	 private Matrix matrix=new Matrix();//矩阵对象 
    	 private Matrix currentMatrix=new Matrix();//存放照片当前的矩阵 
    	 private int mode=0;//确定是放大还是缩小 
    	 private static final int DRAG=1;//拖拉模式 
    	 private static final int ZOOM=2;//缩放模式 
    	 private float startDis;//开始距离 
    	 private PointF midPoint;//中心点 

        boolean recoverActionDefined = false ;
		// 记录连续点击次数
	       private int count = 0;
		private long firstClick;
		private long secondClick;
		private final int totalTime = 50000;

		//参数1:用户触摸的控件；参数2:用户触摸所产生的事件 
    	 public boolean onTouch(View v, MotionEvent event) { 
    	  //判断事件的类型 
    	  //得到低八位才能获取动作，所以要屏蔽高八位(通过与运算&255) 
    	  //ACTION_MASK就是一个常量，代表255 
    	  switch (event.getAction()&MotionEvent.ACTION_MASK) { 
    	  case MotionEvent.ACTION_DOWN://手指下压

			  //当屏幕上已经有触点(手指)，再有一个手指按下屏幕，就会触发这个事件
			  count++;
			  if (1 == count) {
				  firstClick = System.currentTimeMillis();//记录第一次点击时间
			  }
			  else if (2 == count) {
				  secondClick = System.currentTimeMillis();//记录第二次点击时间
				  long nClickGapTime = secondClick - firstClick ;
				  if (nClickGapTime > 100 && nClickGapTime<200 ) {
					  //判断二次点击时间间隔是否在设定的间隔时间之内
					          /*   if (mCallback != null) {
									 mCallback.onDoubleClick();

								 }   */
					  matrix.reset();
					  ImageViewZoomHelper.this.Recover();
                      this.recoverActionDefined = true ;
					  count = 0;
					  firstClick = 0;
				  }
				  else {
					  firstClick = secondClick;
					  count = 1;
				  }
				  secondClick = 0;
			  }
    	  mode=DRAG; 
    	  currentMatrix.set( iv.getImageMatrix());//记录ImageView当前的移动位置 
    	  startPoint.set(event.getX(), event.getY()); 
    	  break; 
    	  case MotionEvent.ACTION_MOVE://手指在屏幕移动，改事件会不断被调用 
    	  if(mode==DRAG){//拖拉模式 
    	   float dx=event.getX()-startPoint.x;//得到在x轴的移动距离 
    	   float dy=event.getY()-startPoint.y;//得到在y轴的移动距离 
    	   matrix.set(currentMatrix);//在没有进行移动之前的位置基础上进行移动 
    	   //实现位置的移动 
    	   matrix.postTranslate(dx, dy); 
    	  }else if(mode==ZOOM){//缩放模式 
    	   float endDis=distance(event);//结束距离 
    	   if(endDis>10f){//防止不规则手指触碰 
    	    //结束距离除以开始距离得到缩放倍数 
    	    float scale=endDis/startDis; 
    	    //通过矩阵实现缩放 
    	    //参数：1.2.指定在xy轴的放大倍数;3,4以哪个参考点进行缩放 
    	    //开始的参考点以两个触摸点的中心为准 
    	    matrix.set(currentMatrix);//在没有进行缩放之前的基础上进行缩放 
    	    matrix.postScale(scale,scale,midPoint.x,midPoint.y); 
    	   } 
    	     
    	  } 
    	    
    	  break; 
    	  case MotionEvent.ACTION_UP://手指离开屏幕 
    	  case MotionEvent.ACTION_POINTER_UP://当屏幕上已经有手指离开屏幕，屏幕上还有一个手指，就会触发这个事件 
    	  mode=0; 
    	  break; 
    	  case MotionEvent.ACTION_POINTER_DOWN:


		 
    	  mode=ZOOM; 
    	  startDis=distance(event); 
    	  if(startDis>10f){//防止不规则手指触碰 
    	   midPoint=mid(event); 
    	   currentMatrix.set(iv.getImageMatrix());//记录ImageView当前的缩放倍数 
    	  } 
    	  break; 
    	    
    	  default: 
    	  break; 
    	  }

    	  //将imageView的矩阵位置改变 
    	  iv.setImageMatrix(matrix);

    	  return true; 
    	 } 
    	   
    	 } 
    	 //计算两点之间的距离(勾股定理) 
    	 public float distance(MotionEvent event) { 
    	  float dx=event.getX(1)-event.getX(0); 
    	  float dy=event.getY(1)-event.getY(0); 
    	  return FloatMath.sqrt(dx*dx+dy*dy); 
    	 } 
    	   
    	 //计算两个点的中心点 
    	 public static PointF mid(MotionEvent event){ 
    	  float midx=(event.getX(1)+event.getX(0))/2; 
    	  float midy=(event.getY(1)+event.getY(0))/2; 
    	  return new PointF(midx,midy); 
    	 }
//切换上下张图像
     public void PerformPreviousNextStep(boolean isPrevious,String [] FileNames)
	 {
		 if ( isPrevious)
		 {

			 if (nIndex_Image >= FileNames.length) {
				 nIndex_Image = FileNames.length - 2;
				 if(nIndex_Image<0)
					 nIndex_Image = 1 ;
			 }
			 if (nIndex_Image  < 0)
				 return;

			 if(nIndex_Image > GetCurrentImageIndex( Source,FileNames))
			 {
				 nIndex_Image-=2 ;
			 }



			  SetImageEx(FileNames[nIndex_Image--]);
		 }
		 else
		 {
			 if (nIndex_Image < 0) {
				 if(FileNames.length>1)
					 nIndex_Image = 1;
				 else
					 nIndex_Image = 0 ;
			 }
			 if (nIndex_Image >= FileNames.length)
				 return;
			 if(nIndex_Image < GetCurrentImageIndex(  Source,FileNames))
			 {
				 nIndex_Image+=2 ;
			 }

			  SetImageEx(FileNames[nIndex_Image++]);

		 }
	 }
	public int GetCurrentImageIndex(String str,String[] fileNames){
		int nIndex = -1 ;
		for (int i = 0; i < fileNames.length; i++) {
			if(fileNames[i].equals( this.Source))
			{
				nIndex = i ;
				break;
			}
		}

		return nIndex ;
	}
 
}
