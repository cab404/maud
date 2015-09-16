package ru.ponyhawks.android.parts;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import ru.ponyhawks.android.R;

/**
 * Well, sorry for no comments here!
 * Still you can send me your question to me@cab404.ru!
 * <p/>
 * Created at 03:46 on 15/09/15
 *
 * @author cab404
 */
public class DrawerEntryPart extends MoonlitPart<DrawerEntryPart.Data> {
    public static class Data {

        public Data(String title, int id) {
            this.title = title;
            this.id = id;
        }

        public Data(String title, int id, int count) {
            this.count = count;
            this.id = id;
            this.title = title;
        }

        public int count, id;
        public String title;
    }

    @Bind(R.id.title)
    TextView title;
    @Bind(R.id.count)
    TextView count;

    @Override
    public void convert(View view, Data data, int index, ViewGroup parent) {
        super.convert(view, data, index, parent);
        ButterKnife.bind(this, view);
        count.setVisibility(data.count == 0 ? View.GONE : View.VISIBLE);
        count.setText(data.count + "");
        title.setText(data.title);
    }

    @Override
    public int getLayoutId() {
        return R.layout.part_drawer_entry;
    }

}
