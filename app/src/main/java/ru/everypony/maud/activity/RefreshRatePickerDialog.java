package ru.everypony.maud.activity;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ru.everypony.maud.R;

/**
 * Well, sorry for no comments here!
 * Still you can send me your question to me@cab404.ru!
 * <p/>
 * Created at 01:30 on 16/10/15
 *
 * @author cab404
 */
public class RefreshRatePickerDialog extends Dialog implements SeekBar.OnSeekBarChangeListener {

    private static final LinkedHashMap<Long, Integer> times = new LinkedHashMap<>();
    private static final List<Long> refreshValues = new ArrayList<>();
    RefreshPickedListener listener;
    SavedRefreshState state;

    public void setListener(RefreshPickedListener listener) {
        this.listener = listener;
    }

    public void setState(SavedRefreshState state) {
        this.state = state;
    }

    static {
        times.put(5000l, R.string.interval_5_seconds);
        times.put(9000l, R.string.interval_9000_ms);
        times.put(10000l, R.string.interval_10_seconds);
        times.put(13000l, R.string.interval_13_seconds);
        times.put(13000l, R.string.interval_15_seconds);
        times.put(30000l, R.string.interval_30_seconds);
        times.put(42000l, R.string.interval_42_seconds);
        times.put(60000l, R.string.interval_1_minute);
        times.put(65536l, R.string.interval_65536_ms);
        times.put(413000l, R.string.interval_413_seconds);
        times.put(TimeUnit.DAYS.toMillis(1), R.string.interval_1_day);
        times.put(TimeUnit.DAYS.toMillis(365 * 1000 + 250), R.string.interval_1000_years);

        refreshValues.addAll(times.keySet());
    }

    public RefreshRatePickerDialog(Context context) {
        super(context);
    }

    @Bind(R.id.value)
    SeekBar value;

    @Bind(R.id.refresh_enabled)
    CheckBox enabled;

    @Bind(R.id.indicator)
    TextView indicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_autorefresh);
        ButterKnife.bind(this);

        value.setMax(refreshValues.size() - 1);
        System.out.println(state.refresh_rate);

        final int val = refreshValues.indexOf(state.refresh_rate);
        value.setProgress(val);
        value.setOnSeekBarChangeListener(this);
        onProgressChanged(value, val, false);

        enabled.setChecked(state.enabled);

        setCancelable(false);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        indicator.setText(times.get(refreshValues.get(progress)));
        System.out.println(state.refresh_rate);
        state.refresh_rate = refreshValues.get(progress);
    }

    public interface RefreshPickedListener {
        void onRefreshRatePicked(boolean enabled, long rate_ms);
    }

    public static class SavedRefreshState {

        private long refresh_rate = 5000;
        private boolean enabled = false;

        public long getRefreshRate() {
            return refresh_rate;
        }

        public boolean isEnabled() {
            return enabled;
        }

    }

    @Override
    public void dismiss() {
        state.enabled = enabled.isChecked();
        super.dismiss();
    }

    @OnClick(R.id.apply)
    void apply() {
        dismiss();
        listener.onRefreshRatePicked(state.enabled, state.refresh_rate);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

}
