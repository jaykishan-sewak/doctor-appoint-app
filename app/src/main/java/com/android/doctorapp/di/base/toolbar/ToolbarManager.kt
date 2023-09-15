package com.android.doctorapp.di.base.toolbar

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.annotation.MenuRes
import androidx.annotation.StringRes
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import com.android.doctorapp.R

class ToolbarManager constructor(
    private var builder: FragmentToolbar,
    private var container: View
) {

    fun changeToolabrvisibility(visibility: Int) {
        if (builder.resId != FragmentToolbar.NO_TOOLBAR) {

            val fragmentToolbar = container.findViewById(builder.resId) as Toolbar
            fragmentToolbar.post() {
                fragmentToolbar.visibility = visibility
            }
        }
    }

    fun updateTitle(title: String) {

        if (builder.resId != FragmentToolbar.NO_TOOLBAR) {
            val fragmentToolbar = container.findViewById(builder.resId) as Toolbar

            // fragmentToolbar.setTitle(title)
            val tv = fragmentToolbar.findViewById<TextView>(R.id.tvTitle)
            tv.text = title
        }
    }

    fun removeNavigationIcon() {

        if (builder.resId != FragmentToolbar.NO_TOOLBAR) {
            val fragmentToolbar = container.findViewById(builder.resId) as Toolbar

            fragmentToolbar.setNavigationIcon(null)
        }
    }

    fun getToolbarHeight(): Int {

        val fragmentToolbar = container.findViewById(builder.resId) as Toolbar

        return fragmentToolbar.height
    }

    fun updateMenu(
        menuId: Int, menuItems: MutableList<Int>,
        menuClicks: MenuItem.OnMenuItemClickListener?
    ) {

        if (builder.resId != FragmentToolbar.NO_TOOLBAR) {
            val fragmentToolbar = container.findViewById(builder.resId) as Toolbar
            if (fragmentToolbar.menu != null) {
                fragmentToolbar.menu.clear()
                fragmentToolbar.invalidate()
            }
            fragmentToolbar.inflateMenu(menuId)

            if (!menuItems.isEmpty() && menuClicks != null) {
                val menu = fragmentToolbar.menu
                for ((_, menuItemId) in menuItems.withIndex()) {

                    val item = (menu.findItem(menuItemId) as MenuItem)
                    item.isVisible = true

                    if (item.actionView != null) {
                        val actionView = item.actionView
                        when (item.itemId) {
                        }
                        val group: ViewGroup? = actionView as? ViewGroup
                        if (group != null) {
                            for (i in 0..group.childCount) {
                                // (group.getChildAt(i) as? ImageView)?.setColorFilter(builder.titleTextColor,android.graphics.PorterDuff.Mode.SRC_IN)
                            }
                        }
                        actionView?.setOnClickListener() { _ ->
                            menu.performIdentifierAction(item.itemId, 0)
                        }
                    }

                    item.setOnMenuItemClickListener(menuClicks)
                }
            }
        }
    }

    fun updateNavigationIcon(navigationIconId: Drawable) {

        if (builder.resId != FragmentToolbar.NO_TOOLBAR) {
            val fragmentToolbar = container.findViewById(builder.resId) as Toolbar

            fragmentToolbar.navigationIcon = navigationIconId
            when (navigationIconId) {
                container.context.getDrawable(R.drawable.ic_back_white) -> fragmentToolbar.navigationContentDescription =
                    container.context.getString(R.string.iconBack)

                else -> fragmentToolbar.navigationContentDescription =
                    container.context.getString(R.string.iconOpenNavigationDrawer)
            }
        }
    }

    fun updateNavigationActionListener(navigationClickListener: View.OnClickListener) {

        if (builder.resId != FragmentToolbar.NO_TOOLBAR) {
            val fragmentToolbar = container.findViewById(builder.resId) as Toolbar

            fragmentToolbar.setNavigationOnClickListener(navigationClickListener)
        }
    }

    @SuppressLint("ResourceType")
    fun prepareToolbar() {
        if (builder.resId != FragmentToolbar.NO_TOOLBAR) {
            val fragmentToolbar = container.findViewById(builder.resId) as Toolbar

            ViewCompat.setTransitionName(fragmentToolbar, "toolbar_element")
            if (builder.title > -1 && !TextUtils.isEmpty(container.context.getString(builder.title))) {
                // fragmentToolbar.setTitle(builder.title)
                // fragmentToolbar.setTitleTextAppearance(container.context,R.style.toolbarTitle)
                val tv = fragmentToolbar.findViewById<TextView>(R.id.tvTitle)
                tv.text = container.context.getString(builder.title)
            }
            if (builder.titleTextColor != -1) {
                // fragmentToolbar.setTitleTextColor(builder.titleTextColor)
                val tv = fragmentToolbar.findViewById<TextView>(R.id.tvTitle)
                tv.setTextColor(builder.titleTextColor)
            }
            if (builder.ivCenterImage != -1) {
                // fragmentToolbar.setTitleTextColor(builder.titleTextColor)
                val iv = fragmentToolbar.findViewById<ImageView>(R.id.ivCenter)
                iv.visibility = View.VISIBLE
                iv.setImageResource(builder.ivCenterImage)
            }
            if (builder.navigationIconId != null) {

                fragmentToolbar.navigationIcon = builder.navigationIconId
                when (builder.navigationIconId) {
                    container.context.getDrawable(R.drawable.ic_back_white) -> fragmentToolbar.navigationContentDescription =
                        container.context.getString(R.string.iconBack)

                    else -> fragmentToolbar.navigationContentDescription =
                        container.context.getString(R.string.iconOpenNavigationDrawer)
                }

                if (builder.navigationClickListener != null) {
                    fragmentToolbar.setNavigationOnClickListener(builder.navigationClickListener)
                }
            }

            if (builder.menuId != -1) {
                fragmentToolbar.inflateMenu(builder.menuId)
            }
            if (builder.toolbarColor != -1) {
                fragmentToolbar.setBackgroundColor(builder.toolbarColor)
            }
            if (builder.toolbarBackGround != -1) {
                fragmentToolbar.background =
                    container.context.getDrawable(builder.toolbarBackGround)
            }

            if (builder.menuItems.isNotEmpty() && builder.menuClicks != null) {
                val menu = fragmentToolbar.menu
                for ((_, menuItemId) in builder.menuItems.withIndex()) {

                    val item = (menu.findItem(menuItemId) as MenuItem)
                    item.isVisible = true
                    if (item.actionView != null) {
                        val actionView = item.actionView

                        when (item.itemId) {

                        }

                        val group: ViewGroup? = actionView as? ViewGroup
                        if (group != null) {
                            for (i in 0..group.childCount) {
                                (group.getChildAt(i) as? ImageView)?.setColorFilter(
                                    builder.titleTextColor,
                                    android.graphics.PorterDuff.Mode.SRC_IN
                                )
                            }
                        }
                        actionView?.setOnClickListener() { _ ->
                            menu.performIdentifierAction(item.itemId, 0)
                        }
                    }

                    item.setOnMenuItemClickListener(builder.menuClicks)
                }
            }
            if (!TextUtils.isEmpty(builder.titleString)) {
                // fragmentToolbar.setTitle(builder.title)
                // fragmentToolbar.setTitleTextAppearance(container.context,R.style.toolbarTitle)
                val tv = fragmentToolbar.findViewById<TextView>(R.id.tvTitle)
                tv.text = builder.titleString
            }
        }
    }
}

class FragmentToolbar(
    @IdRes val resId: Int,
    @StringRes val title: Int,
    @StringRes val subTitle: Int,
    @IdRes val toolbarColor: Int,
    @IdRes val toolbarBackGround: Int,
    @IdRes val titleTextColor: Int,
    @IdRes val subTitleTextColor: Int,
    val navigationIconId: Drawable?,
    @MenuRes val menuId: Int,
    @IdRes val ivCenterImage: Int,
    val navigationClickListener: View.OnClickListener?,
    val menuItems: MutableList<Int>,
    val menuClicks: MenuItem.OnMenuItemClickListener?,
    val titleString: String
) {

    companion object {
        @JvmField
        val NO_TOOLBAR = -1
    }

    class Builder {
        private var resId: Int = -1
        private var navigationIconId: Drawable? = null
        private var titleTextColor: Int = -1
        private var toolbarColor: Int = -1
        private var toolbarBackGround: Int = -1

        private var subTitleTextColor: Int = -1
        private var menuId: Int = -1
        private var title: Int = -1
        private var subTitle: Int = -1
        private var menuItems: MutableList<Int> = mutableListOf()
        private var menuClicks: MenuItem.OnMenuItemClickListener? = null
        private var navigationClickListener: View.OnClickListener? = null
        private var ivImage: Int = -1
        private var titleString: String = ""

        fun withId(@IdRes resId: Int) = apply { this.resId = resId }
        fun withNavigationIcon(navigationIconId: Drawable?) =
            apply { this.navigationIconId = navigationIconId }

        fun withNavigationListener(navigationClickListener: View.OnClickListener) =
            apply { this.navigationClickListener = navigationClickListener }

        fun withTitleColorId(titleTextColor: Int) = apply { this.titleTextColor = titleTextColor }
        fun withToolbarColorId(toolbarColorId: Int) = apply { this.toolbarColor = toolbarColorId }
        fun withToolbarBackGroundId(toolbarBackGround: Int) =
            apply { this.toolbarColor = toolbarBackGround }

        fun withTitle(title: Int) = apply { this.title = title }

        fun withMenu(@MenuRes menuId: Int) = apply { this.menuId = menuId }

        fun withMenuItems(menuItems: List<Int>, menuClicks: MenuItem.OnMenuItemClickListener?) =
            apply {
                this.menuItems.addAll(menuItems)
                this.menuClicks = menuClicks
            }

        fun withCenterImage(@DrawableRes ivImage: Int) = apply { this.ivImage = ivImage }

        fun withTitleString(title: String) = apply { this.titleString = title }
        fun build() = FragmentToolbar(
            resId,
            title,
            subTitle,
            toolbarColor,
            toolbarBackGround,
            titleTextColor,
            subTitleTextColor,
            navigationIconId,
            menuId,
            ivImage,
            navigationClickListener,
            menuItems,
            menuClicks,
            titleString
        )
    }
}