package ru.everypony.maud.parts;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cab404.chumroll.ChumrollAdapter;
import com.cab404.chumroll.ViewConverter;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.everypony.maud.R;
import ru.everypony.maud.utils.DrawerItemData;

/**
 * Well, sorry for no comments here!
 * Still you can send me your question to me@cab404.ru!
 * <p/>
 * Created at 07:18 on 04/07/16
 *
 * @author cab404
 */
public class DrawerItemConfigPart implements ViewConverter<DrawerItemData> {
    @BindView(R.id.delete)
    View delete;
    @BindView(R.id.title)
    TextView title;

    @Override
    public void convert(View view, final DrawerItemData data, final int index, ViewGroup parent, final ChumrollAdapter adapter) {
        ButterKnife.bind(this, view);
        title.setText(data.name);
        delete.setVisibility(data.deletable ? View.VISIBLE : View.GONE);
        final int id = adapter.idOf(index);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.removeById(id);
            }
        });
    }

    @Override
    public View createView(LayoutInflater inflater, ViewGroup parent, ChumrollAdapter adapter) {
        return inflater.inflate(R.layout.part_drawer_selector_config, parent, false);
    }

    @Override
    public boolean enabled(DrawerItemData data, int index, ChumrollAdapter adapter) {
        return false;
    }
}
