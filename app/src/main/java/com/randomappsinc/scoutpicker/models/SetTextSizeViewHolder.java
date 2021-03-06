package com.randomappsinc.scoutpicker.models;

import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import com.randomappsinc.scoutpicker.R;
import com.randomappsinc.scoutpicker.utils.PreferencesManager;
import com.rey.material.widget.Slider;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SetTextSizeViewHolder {

    @BindView(R.id.text_size_slider) public Slider textSizeSlider;
    @BindView(R.id.sample_text) public TextView sampleText;

    public SetTextSizeViewHolder(View view) {
        ButterKnife.bind(this, view);
        textSizeSlider.setOnPositionChangeListener(sizeSetListener);
        textSizeSlider.setValue((float) PreferencesManager.get().getPresentationTextSize(), false);
    }

    Slider.OnPositionChangeListener sizeSetListener = new Slider.OnPositionChangeListener() {
        @Override
        public void onPositionChanged(Slider view, boolean fromUser, float oldPos,
                                      float newPos, int oldValue, int newValue) {
            sampleText.setTextSize(TypedValue.COMPLEX_UNIT_SP, newValue * 8);
        }
    };

    public void revertSetting() {
        textSizeSlider.setValue((float) PreferencesManager.get().getPresentationTextSize(), false);
    }
}
