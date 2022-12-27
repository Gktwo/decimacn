package com.shade.decima.ui.menu.menus;

import com.shade.decima.BuildConfig;
import com.shade.decima.ui.Application;
import com.shade.platform.model.util.IOUtils;
import com.shade.platform.ui.menus.MenuItem;
import com.shade.platform.ui.menus.MenuItemContext;
import com.shade.platform.ui.menus.MenuItemRegistration;
import com.shade.util.NotNull;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;
import java.text.MessageFormat;
import java.util.Properties;

import static com.shade.decima.ui.menu.MenuConstants.*;

public interface HelpMenu {
    @MenuItemRegistration(parent = APP_MENU_HELP_ID, name = "&关于", group = APP_MENU_HELP_GROUP_ABOUT, order = 1000)
    class AboutItem extends MenuItem {
        private static final MessageFormat MESSAGE = new MessageFormat("""
            <h1>{0}</h1>
           Decima引擎支持的游戏中查看和编辑数据的工具.
            <br><br>
            <table>
            <tr><td><b>Version:</b></td><td>{1} (Built on {2,date,short}), commit: <a href="https://github.com/ShadelessFox/decima/commit/{3}">{3}</a></tr>
            <tr><td><b>VM Version:</b></td><td>{4}; {5} ({6} {7})</td></tr>
            <tr><td><b>VM Vendor:</b></td><td>{8}, <a href="{9}">{9}</a></td></tr>
            <tr><td><b>中文翻译:</b></td><td>@gktwo,  <a href="https://github.com/gktwo/">https://github.com/gktwo/</a></tr> 
            <br> 
             </table>
            <tr><td><b>查看更多信息</b> <a href="https://github.com/ShadelessFox/decima">https://github.com/ShadelessFox/decima</a> </tr>  
            
            """);

        @Override
        public void perform(@NotNull MenuItemContext ctx) {
            final Properties p = System.getProperties();

            final JEditorPane pane = new JEditorPane();
            pane.setEditorKit(new HTMLEditorKit());
            pane.setEditable(false);
            pane.addHyperlinkListener(e -> {
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    IOUtils.unchecked(() -> {
                        Desktop.getDesktop().browse(e.getURL().toURI());
                        return null;
                    });
                }
            });
            pane.setText(MESSAGE.format(new Object[]{
                BuildConfig.APP_TITLE,
                BuildConfig.APP_VERSION, BuildConfig.BUILD_TIME, BuildConfig.BUILD_COMMIT,
                p.get("java.version"), p.get("java.vm.name"), p.get("java.vm.version"), p.get("java.vm.info"),
                p.get("java.vendor"), p.get("java.vendor.url")
            }));

            JOptionPane.showMessageDialog(
                Application.getFrame(),
                pane,
                "About",
                JOptionPane.PLAIN_MESSAGE
            );

        }
    }
}
