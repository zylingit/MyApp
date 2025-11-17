package com.aam.mida.mida_yk.widgets;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialog;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.aam.mida.mida_yk.GlideApp;
import com.aam.mida.mida_yk.R;
import com.aam.mida.mida_yk.entity.FileInfoEntity;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

/**
 * 图片全屏样式
 */
public class PhotoShowDialog extends AppCompatDialog {
    private Context mContext;
    private List<FileInfoEntity> photoLists;
    private int mPosition;
    private ViewPager vp;
//    private TextView tv;

    public PhotoShowDialog(@NonNull Context context) {
        super(context);
        mContext = context;
    }

    public PhotoShowDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        mContext = context;
    }


    public PhotoShowDialog(Context context, List<FileInfoEntity> photoLists, int position) {
        this(context, R.style.Pic_Dialog);
        this.photoLists = photoLists;
        this.mPosition = position;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_dialog);
        init();
    }

    private void init() {
        getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        vp = findViewById(R.id.vp);
//        tv = findViewById(R.id.tv);
        assert vp != null;
        vp.setAdapter(new VpAdapter(mContext));
        vp.setCurrentItem(mPosition);
//        tv.setText(vp.getCurrentItem() + 1 + "/" + photoLists.size());
//        tv.setVisibility(photoLists.size() == 1 ? View.INVISIBLE : View.VISIBLE);
        vp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
//                tv.setText(position + 1 + "/" + photoLists.size());
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    class VpAdapter extends PagerAdapter {
        Context context;

        public VpAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() {
            return photoLists.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = View.inflate(context, R.layout.item, null);
            AppCompatImageView photoView = view.findViewById(R.id.photo);
            RequestOptions options = new RequestOptions();
            FileInfoEntity entity = photoLists.get(position);
            if (entity.getCookieStr() == null || entity.getCookieStr().isEmpty()) {
                GlideApp.with(context)
                        .load(entity.getName())
                        .apply(options)
                        .transition(new DrawableTransitionOptions().crossFade())
                        .into(photoView);
            } else {
                GlideUrl glideUrl = new GlideUrl(entity.getName(), new LazyHeaders.Builder()
                        .addHeader("Cookie", entity.getCookieStr())
                        .build());
                GlideApp.with(context)
                        .load(glideUrl)
                        .apply(options)
                        .transition(new DrawableTransitionOptions().crossFade())
                        .into(photoView);
            }

            ((ViewPager) container).addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            ((ViewPager) container).removeView((View) object);
        }
    }
}
