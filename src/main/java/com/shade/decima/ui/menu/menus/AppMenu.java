package com.shade.decima.ui.menu.menus;

import com.shade.platform.ui.menus.Menu;
import com.shade.platform.ui.menus.MenuRegistration;

import static com.shade.decima.ui.menu.MenuConstants.*;

public interface AppMenu {
    @MenuRegistration(id = APP_MENU_FILE_ID, name = "&文件", order = 1000)
    class FileMenu extends Menu {}

    @MenuRegistration(id = APP_MENU_EDIT_ID, name = "&编辑", order = 2000)
    class EditMenu extends Menu {}

    @MenuRegistration(id = APP_MENU_VIEW_ID, name = "&查看", order = 3000)
    class ViewMenu extends Menu {}

    @MenuRegistration(id = APP_MENU_HELP_ID, name = "&帮助", order = 4000)
    class HelpMenu extends Menu {}
}
