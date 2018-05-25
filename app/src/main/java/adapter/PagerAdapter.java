package adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.example.sasiroot.agpr.Autopilot;
import com.example.sasiroot.agpr.MenuSlider;
import com.example.sasiroot.agpr.Telemetria;

public class PagerAdapter extends FragmentStatePagerAdapter {
    int numOfTabs ;

    public PagerAdapter(FragmentManager fm, int numOfTabs){
        super(fm);
        this.numOfTabs=numOfTabs;
    }


    @Override
    public Fragment getItem(int position){
            switch (position) {
                case 0 :
                    Autopilot page1 = new Autopilot();
                    return page1;
                case 1 :
                    MenuSlider page2 = new MenuSlider();
                    return page2;
                case 2 :
                    Telemetria page3 = new Telemetria();
                    return page3;
                default:
                    return null;
            }
    }


    @Override
    public int getCount(){
        return numOfTabs;
    }

}
