package com.android.app.fakechatapp.utils

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.appbar.AppBarLayout

class ScrollingViewBehavior(context: Context, attrs: AttributeSet) :
    AppBarLayout.ScrollingViewBehavior(context, attrs) {

    override fun onDependentViewChanged(
        parent: CoordinatorLayout,
        child: View,
        dependency: View
    ): Boolean {
        val topMargin = (dependency.layoutParams as CoordinatorLayout.LayoutParams).topMargin
        val distanceToScroll = dependency.top - child.height - topMargin
        child.translationY = -distanceToScroll.toFloat()
        return true
    }
}

/*
* <androidx.coordinatorlayout.widget.CoordinatorLayout
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:layout_above="@id/mainLayout"
            android:layout_below="@id/toolbar">

            <androidx.core.widget.NestedScrollView
                android:layout_width="match_parent"
                android:layout_height="match_parent"
                android:fillViewport="true"
                app:layout_behavior="@string/appbar_scrolling_view_behavior">

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="vertical">

                    <com.google.android.material.appbar.AppBarLayout
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:orientation="vertical">

                        <LinearLayout
                            android:id="@+id/fixedViewsContainer"
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:orientation="vertical">

                            <TextView
                                android:id="@+id/tvDate"
                                android:layout_width="wrap_content"
                                android:layout_height="wrap_content"
                                android:layout_gravity="center"
                                android:layout_marginTop="10dp"
                                android:background="@drawable/date_bg"
                                android:padding="5dp"
                                android:text="March 25. 2024"
                                android:textColor="@color/white"
                                android:textStyle="bold" />

                            <TextView
                                android:id="@+id/tvEncryption"
                                android:layout_width="match_parent"
                                android:layout_height="wrap_content"
                                android:layout_marginStart="20dp"
                                android:layout_marginTop="10dp"
                                android:layout_marginEnd="20dp"
                                android:background="@drawable/date_bg"
                                android:gravity="center"
                                android:padding="5dp"
                                android:text="Encryption text here"
                                android:textColor="@color/yellow"
                                android:textStyle="bold" />

                        </LinearLayout>

                    </com.google.android.material.appbar.AppBarLayout>

                    <androidx.recyclerview.widget.RecyclerView
                        android:id="@+id/chatRecyclerview"
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        tools:itemCount="3"
                        tools:listitem="@layout/item_chat_right" />

                </LinearLayout>

            </androidx.core.widget.NestedScrollView>

        </androidx.coordinatorlayout.widget.CoordinatorLayout>
* */