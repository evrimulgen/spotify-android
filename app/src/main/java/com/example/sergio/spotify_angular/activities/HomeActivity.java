package com.example.sergio.spotify_angular.activities;


import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.devspark.appmsg.AppMsg;
import com.example.sergio.spotify_angular.R;
import com.example.sergio.spotify_angular.events.ApiErrorEvent;
import com.example.sergio.spotify_angular.events.MenuItemSelected;
import com.example.sergio.spotify_angular.events.LoadProfileEvent;
import com.example.sergio.spotify_angular.events.PlaylistSelectedEvent;
import com.example.sergio.spotify_angular.events.ProfileLoadedEvent;
import com.example.sergio.spotify_angular.events.SeeAllResultsEvent;
import com.example.sergio.spotify_angular.fragments.ExplorerFragment;
import com.example.sergio.spotify_angular.fragments.NewReleasesFragment;
import com.example.sergio.spotify_angular.fragments.PlaylistPreviewFragment;
import com.example.sergio.spotify_angular.fragments.SearchFragment;
import com.example.sergio.spotify_angular.fragments.YourLibraryFragment;
import com.example.sergio.spotify_angular.fragments.resultsearch.AbstractEndlessScrollFragment;
import com.example.sergio.spotify_angular.fragments.resultsearch.AbstractShowResultsFragment;
import com.example.sergio.spotify_angular.fragments.resultsearch.AlbumsFragment;
import com.example.sergio.spotify_angular.fragments.resultsearch.ArtistFragment;
import com.example.sergio.spotify_angular.fragments.resultsearch.MyPlaylistsFragment;
import com.example.sergio.spotify_angular.fragments.resultsearch.PlaylistFragment;
import com.example.sergio.spotify_angular.fragments.resultsearch.TracksFragment;
import com.example.sergio.spotify_angular.utils.AppHelpers;
import com.example.sergio.spotify_angular.utils.ImageUtils;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import de.hdodenhof.circleimageview.CircleImageView;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class HomeActivity extends AppCompatActivity  {

    private final static String CURRENT_TITLE = "title";
    private DrawerLayout mDrawer;
    private Toolbar toolbar;
    private ActionBarDrawerToggle drawerToggle;
    private EventBus bus = EventBus.getDefault();
    private NavigationView nvDrawer;


    public void selectDrawerItem(MenuItem menuItem) {
        // Create a new fragment and specify the fragment to show based on nav item clicked
        Fragment fragment;
        switch(menuItem.getItemId()) {
            case R.id.nav_search:
                fragment = new SearchFragment();
                break;
            case R.id.nav_explorer:
                fragment =  new ExplorerFragment();
                break;
            case R.id.nav_your_music:
                fragment = new YourLibraryFragment();
                break;
            default:
                fragment =  new PlaylistPreviewFragment();
        }


        AppHelpers.setFragment(this,fragment, R.id.flContent, true,true);

        // Highlight the selected item has been done by NavigationView
        menuItem.setChecked(true);
        // Set action bar title
        setTitle(menuItem.getTitle());
        // Close the navigation drawer
        mDrawer.closeDrawers();

    }

    private ActionBarDrawerToggle setupDrawerToggle() {
        return new ActionBarDrawerToggle(this, mDrawer, toolbar, R.string.drawer_open,  R.string.drawer_close);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_main);


        // Set a Toolbar to replace the ActionBar.
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        // Find our drawer view
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerToggle = setupDrawerToggle();

        // Tie DrawerLayout events to the ActionBarToggle
        mDrawer.addDrawerListener(drawerToggle);
        // Find our drawer view
        nvDrawer = (NavigationView) findViewById(R.id.nvView);
        // Setup drawer view
        nvDrawer.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        selectDrawerItem(menuItem);
                        return true;
                    }
                });


        if (savedInstanceState == null)
            selectDrawerItem(nvDrawer.getMenu().getItem(1));
        else
            setTitle(savedInstanceState.getString(CURRENT_TITLE));

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // The action bar home/up action should open or close the drawer.
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawer.openDrawer(GravityCompat.START);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(CURRENT_TITLE, getTitle().toString());
    }

    // `onPostCreate` called when activity start-up is complete after `onStart()`
    // NOTE! Make sure to override the method with only a single `Bundle` argument
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onResume() {
        super.onResume();
        bus.register(this);
        bus.post(new LoadProfileEvent());
    }

    @Override
    protected void onPause() {
        super.onPause();
        bus.unregister(this);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }



    @Subscribe
    public void onProfileLoaded(ProfileLoadedEvent event){
        View headerLayout = nvDrawer.getHeaderView(0);
        final FrameLayout header = (FrameLayout) findViewById(R.id.header_content);
        TextView name = (TextView)headerLayout.findViewById(R.id.profile_name);
        name.setText(event.getUser().display_name);
        CircleImageView photo = (CircleImageView)headerLayout.findViewById(R.id.profile_image);
        String url = event.getUser().images.get(0).url;
        Picasso.with(this).load(url).noFade().into(photo);
        ImageUtils.getBlurredImage(this,url,"profile_user",10, new ImageUtils.BlurEffectListener(){

            @Override
            public void onDone(Bitmap bitmap) {
                header.setBackground(new BitmapDrawable(getResources(),bitmap));
            }
        } );
    }

    @Subscribe
    public void onMenuItemSelected(MenuItemSelected event){

        Fragment fragment = null;
        switch (event.getId()){
            case R.id.new_releases:
                fragment = new NewReleasesFragment();
                setTitle(getString(R.string.new_releases_fragment_title));
                break;
            case R.id.library_playlist:
                fragment = new MyPlaylistsFragment();
                setTitle(getString(R.string.my_playlists));
                break;
            default:
                Toast.makeText(this,"Opción no implementada", Toast.LENGTH_LONG).show();
        }

        if (fragment != null) AppHelpers.setFragment(this,fragment, R.id.flContent, true,true);
    }

    @Subscribe
    public void onPlaylistSelected(PlaylistSelectedEvent event){
        Bundle bundle = new Bundle();
        bundle.putString(PlaylistPreviewFragment.PLAYLIST_ID_PARAM, event.getPlaylist().id);
        bundle.putString(PlaylistPreviewFragment.PLAYLIST_OWNER_ID_PARAM, event.getPlaylist().owner.id);
        PlaylistPreviewFragment fragment = new PlaylistPreviewFragment();
        fragment.setArguments(bundle);
        AppHelpers.setFragment(this,fragment, R.id.flContent, true,true);
        setTitle(event.getPlaylist().name);
    }

    @Subscribe
    public void onSeeAllResults(SeeAllResultsEvent event){
        Class<? extends AbstractShowResultsFragment> fragmentClass;
        switch (event.getType()){
            case ARTISTS:
                fragmentClass = ArtistFragment.class;
                break;
            case ALBUMS:
                fragmentClass = AlbumsFragment.class;
                break;
            case TRACKS:
                fragmentClass = TracksFragment.class;
                break;
            case PLAYLIST:
                fragmentClass = PlaylistFragment.class;
                break;
            default:
                fragmentClass = ArtistFragment.class;
        }
        try {
            AppHelpers.setFragment(this,AbstractShowResultsFragment.newInstance(event.getText(),fragmentClass), R.id.flContent, true,true);
            setTitle(event.getTitle());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }

    }

    @Subscribe
    public void onApiError(ApiErrorEvent event) {
        AppMsg.Style style;
        switch (event.getType()){
            case ALERT:
                style = AppMsg.STYLE_ALERT;
                break;
            case INFO:
                style = AppMsg.STYLE_INFO;
                break;
            case WARNING:
                style = AppMsg.STYLE_CONFIRM;
                break;
            default:
                style = AppMsg.STYLE_ALERT;
        }

        AppMsg msg = AppMsg.makeText(this, event.getMessage(), style);
        msg.setParent(R.id.flContent);
        msg.show();
    }

    public void clearToolbar(){
        toolbar.getMenu().clear();
    }


}
