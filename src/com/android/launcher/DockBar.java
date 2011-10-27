package com.android.launcher;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;

public class DockBar extends LinearLayout implements OnClickListener {
    public static final int TOP=0;
    public static final int BOTTOM=1;
    public static final int LEFT=2;
    public static final int RIGHT=3;
    
    private static final int OPEN=0;
    private static final int CLOSED=1;
    private static final int ANIM_DURATION = 250;
	private int mHandleId;
    private int mContentId;
    private View mHandle;
    private int mState=CLOSED;
    private int mPosition=TOP;
    private boolean mFirstLayout=true;
    //private View mContent;
    private GestureDetector mGestureDetector;
    private DockbarGestureListener mGestureListener;
	public boolean mInterceptClicks=false;
	private DockBarListener mDockBarListener;
	public DockBar(Context context) {
		super(context);
	}

	public DockBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DockBar);
        mHandleId = a.getResourceId(R.styleable.DockBar_handle, 0);
        /*if (mHandleId == 0) {
                e = new IllegalArgumentException(a.getPositionDescription() +
                                ": The handle attribute is required and must refer to a valid child.");
        }*/
        mContentId = a.getResourceId(R.styleable.DockBar_content, 0);
        if (mContentId == 0) {
            new IllegalArgumentException(a.getPositionDescription() +
                                ": The content attribute is required and must refer to a valid child.");
        }
        mPosition = a.getInt(
                R.styleable.DockBar_position, mPosition);
        a.recycle();
        mGestureListener = new DockbarGestureListener();
        //mGestureDetector = new GestureDetector(mGestureListener);
        mGestureDetector = new GestureDetector(context, mGestureListener);
	}
    @Override
    protected void onFinishInflate() {
    	super.onFinishInflate();
    	if(mHandleId!=0){
    		mHandle = findViewById(mHandleId);
	    	/*if (mHandle == null) {
	    		String name = getResources().getResourceEntryName(mHandleId);
	    		throw new RuntimeException("Your DockBar must have a child View whose id attribute is 'R.id." + name + "'");
	    	}*/
    		if(mHandle!=null)
    			mHandle.setOnClickListener(this);
    	}
    	setVisibility(VISIBLE);
    }
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO Auto-generated method stub
		super.onLayout(changed, l, t, r, b);
		if(mFirstLayout){
			setVisibility(GONE);
			mFirstLayout=false;
		}
	}
	public void open(){
		dispatchDockBarEvent(true);
		setClickable(false);
		mState=OPEN;
		setVisibility(View.VISIBLE);
        int x=0;
        int y=0;
		switch (mPosition) {
	        case LEFT:
	            x=getWidth();
	            break;
	        case RIGHT:
	            x=getWidth();
	            break;
	        case TOP:
	            y=-getHeight();
	            break;
	        case BOTTOM:
	            y=getHeight();
	            break;
		}
		TranslateAnimation anim=new TranslateAnimation(x, 0,y, 0);
		anim.setDuration(ANIM_DURATION);
		startAnimation(anim);
	}
	public void close(){
		dispatchDockBarEvent(false);
		mState=CLOSED;
        int x=0;
        int y=0;
		switch (mPosition) {
	        case LEFT:
	            x=-getWidth();
	            break;
	        case RIGHT:
	            x=getWidth();
	            break;
	        case TOP:
	            y=-getHeight();
	            break;
	        case BOTTOM:
	            y=getHeight();
	            break;
		}		
		TranslateAnimation anim=new TranslateAnimation(0, x, 0,y);
		anim.setDuration(ANIM_DURATION);
		anim.setAnimationListener(new AnimationListener() {
			
			//@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
			}
			
			//@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub
			}
			
			//@Override
			public void onAnimationEnd(Animation animation) {
				setVisibility(View.GONE);
			}
		});
		startAnimation(anim);
	}
	public boolean isOpen(){
		return (mState==OPEN);
	}
	public int getSize(){
		if(mPosition==TOP || mPosition==BOTTOM){
			return getHeight();
		}else{
			return getWidth();
		}
	}
	@Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mGestureDetector.onTouchEvent(ev)) {
            return true;
        }
        if(!mInterceptClicks){
	        return super.onInterceptTouchEvent(ev);
        }
        return true;
    }	
	@Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mGestureDetector.onTouchEvent(ev)) {
            return true;
        }
        if(!mInterceptClicks){
	        return super.onTouchEvent(ev);
        }
        return false;
    }
	
	class DockbarGestureListener implements OnGestureListener {
        public boolean onDown(MotionEvent e) {
        	mInterceptClicks=false;
        	return false;
        }
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float velocity=0;
    		switch (mPosition) {
		        case LEFT:
		        	if(velocityX<0 && Math.abs(velocityY)<Math.abs(velocityX))
		        		velocity=Math.abs(velocityX);
		        	break;
		        case RIGHT:
		        	if(velocityX>0 && Math.abs(velocityY)<Math.abs(velocityX))
		        		velocity=Math.abs(velocityX);
		            break;
		        case TOP:
		        	if(velocityY<0 && Math.abs(velocityY)>Math.abs(velocityX))
		        		velocity=Math.abs(velocityY);
		        	break;
		        case BOTTOM:
		        	if(velocityY>0 && Math.abs(velocityY)>Math.abs(velocityX))
		        		velocity=Math.abs(velocityY);
		            break;
    		}
            if(velocity>0){
            	close();
            	mInterceptClicks=true;
            	
            	return true;
            }
            return false;
        }
        public void onLongPress(MotionEvent e) {
        	//not used
        }
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        	//not used
    		return false;
        }
        public void onShowPress(MotionEvent e) {
        	//not used
        }
        public boolean onSingleTapUp(MotionEvent e) {
        	//not used
            return false;
        }
	}

	//@Override
	public void onClick(View v) {
		if(v==mHandle){
			close();
		}
	}
    public void setDockBarListener(DockBarListener listener) {
        mDockBarListener = listener;
    }

    /**
     * Dispatches a trigger event to listener. Ignored if a listener is not set.
     * @param whichHandle the handle that triggered the event.
     */
    private void dispatchDockBarEvent(boolean open) {
        if (mDockBarListener != null) {
        	if(open){
        		mDockBarListener.onOpen();
        	}else{
        		mDockBarListener.onClose();
        	}
        }
    }	
    /**
     * Interface definition for a callback to be invoked when a tab is triggered
     * by moving it beyond a threshold.
     */
    public interface DockBarListener {
        void onOpen();
        void onClose();
    }
	
}
