package com.aam.mida.mida_yk.widgets

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import com.aam.mida.mida_yk.R
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.interfaces.DraweeController
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.imagepipeline.common.ResizeOptions
import com.facebook.imagepipeline.request.ImageRequestBuilder


/**
 * 在小控件加载大图情况下优化内存占用效率
 * 使用这两个需给定具体值，android:layout_width="具体大小"
 *       android:layout_height="具体大小"
 */
class LowMemorySimpleDraweeView : SimpleDraweeView {

    private var mUri: Uri? = null
    private var mCallerContext: Any? = null
    private var mWidth: Int = -1
    private var mHeight: Int = -1


    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(
        context,
        attrs,
        defStyleAttr,
        0
    )

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {

        val typedArray =
            context.obtainStyledAttributes(attrs, R.styleable.LowMemorySimpleDraweeView)
        mWidth = typedArray.getLayoutDimension(
            R.styleable.LowMemorySimpleDraweeView_android_layout_width,
            -1
        )
        mHeight = typedArray.getLayoutDimension(
            R.styleable.LowMemorySimpleDraweeView_android_layout_height,
            -1
        )
        //LogUtils.e("LowMemoryView", "mWidth $mWidth mHeight $mHeight")
        typedArray.recycle()

    }

    override fun setImageURI(uri: Uri?, callerContext: Any?) {
        mUri = uri
        mCallerContext = callerContext
        if (mWidth > 0 && mHeight > 0) {
            setImageURI(mWidth, mHeight)
        }

    }

    override fun setImageURI(uriString: String?) {
        mUri = if (uriString != null) Uri.parse(uriString) else null
        if (mWidth > 0 && mHeight > 0) {
            setImageURI(mWidth, mHeight)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mWidth = w
        mHeight = h
    }


    private fun setImageURI(width: Int, height: Int) {
        mUri?.let {
            val imageRequest = ImageRequestBuilder.newBuilderWithSource(mUri)
                .setResizeOptions(ResizeOptions(width, height))
                .setProgressiveRenderingEnabled(true)
                .build()
            val controller: DraweeController = Fresco.newDraweeControllerBuilder()
                .setCallerContext(mCallerContext)
                .setOldController(controller)
                .setImageRequest(imageRequest)
                .build()
            setController(controller)
        }
    }

    fun setImageURI(uriString: String, width: Int, height: Int) {
        val imageRequest = ImageRequestBuilder.newBuilderWithSource(Uri.parse(uriString))
            .setResizeOptions(ResizeOptions(width, height))
            .setProgressiveRenderingEnabled(true)
            .build()
        val controller: DraweeController = Fresco.newDraweeControllerBuilder()
            .setCallerContext(mCallerContext)
            .setOldController(controller)
            .setImageRequest(imageRequest)
            .build()
        setController(controller)
    }

    override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
        super.onWindowFocusChanged(hasWindowFocus)
        if (hasWindowFocus) {
            mWidth = width
            mHeight = height
            if (mWidth > 0 && mHeight > 0) {
                setImageURI(mWidth, mHeight)
            }
        }
    }

}