package vn.androidhaui.foxtrip.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import vn.androidhaui.foxtrip.R;
import vn.androidhaui.foxtrip.adapters.HistoriesAdapter;
import vn.androidhaui.foxtrip.databinding.FragmentHistoryListBinding;
import vn.androidhaui.foxtrip.viewmodels.HistoriesViewModel;

public class HistoriesFragment extends Fragment {
    private FragmentHistoryListBinding binding;
    private HistoriesViewModel vm;
    private HistoriesAdapter adapter;

    public static HistoriesFragment newInstance() { return new HistoriesFragment(); }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHistoryListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        vm = new ViewModelProvider(this).get(HistoriesViewModel.class);

        adapter = new HistoriesAdapter(history ->
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, HistoryDetailFragment.newInstance(history.get_id()))
                    .addToBackStack(null)
                    .commit()
        );

        binding.recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recycler.setAdapter(adapter);

        vm.getHistories().observe(getViewLifecycleOwner(), histories -> adapter.setItems(histories));
        vm.isLoading().observe(getViewLifecycleOwner(), loading -> binding.progress.setVisibility(loading ? View.VISIBLE : View.GONE));
        vm.getMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) binding.tvEmpty.setText(msg);
        });

        vm.loadHistories();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
