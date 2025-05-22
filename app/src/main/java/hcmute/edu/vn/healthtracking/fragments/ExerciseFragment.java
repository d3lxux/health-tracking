package hcmute.edu.vn.healthtracking.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import hcmute.edu.vn.healthtracking.R;

public class ExerciseFragment extends Fragment {
    
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private final String[] tabTitles = new String[]{"Chạy bộ", "Đạp xe"};
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_workout, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize views
        tabLayout = view.findViewById(R.id.tab_layout);
        viewPager = view.findViewById(R.id.view_pager);
        
        // Setup ViewPager with adapter
        WorkoutPagerAdapter pagerAdapter = new WorkoutPagerAdapter(requireActivity());
        viewPager.setAdapter(pagerAdapter);
        
        // Connect TabLayout with ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(tabTitles[position]);
        }).attach();
    }
    
    // Adapter for the ViewPager
    private static class WorkoutPagerAdapter extends FragmentStateAdapter {
        
        public WorkoutPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }
        
        @NonNull
        @Override
        public Fragment createFragment(int position) {
            // Return the fragment for the corresponding tab
            return position == 0 ? new RunningFragment() : new CyclingFragment();
        }
        
        @Override
        public int getItemCount() {
            return 2; // We have 2 tabs: Running and Cycling
        }
    }
}
