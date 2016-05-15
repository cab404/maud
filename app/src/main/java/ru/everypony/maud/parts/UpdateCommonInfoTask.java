package ru.everypony.maud.parts;

import com.cab404.libtabun.data.CommonInfo;
import com.cab404.libtabun.pages.MainPage;
import com.cab404.moonlight.framework.ModularBlockParser;

import ru.everypony.maud.statics.Providers;

/**
 * Well, sorry for no comments here!
 * Still you can send me your question to me@cab404.ru!
 * <p/>
 * Created at 11:38 on 23/09/15
 *
 * @author cab404
 */
public class UpdateCommonInfoTask implements ModularBlockParser.ParsedObjectHandler {

    @Override
    public void handle(Object object, int key) {
        if (MainPage.BLOCK_COMMON_INFO == key) {
            Providers.UserInfo.getInstance().setInfo(((CommonInfo) object));
            System.out.println("gotit " + ((CommonInfo) object).new_messages);
        }
    }

}
