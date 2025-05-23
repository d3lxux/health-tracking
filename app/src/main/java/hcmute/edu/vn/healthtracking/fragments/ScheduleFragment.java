package hcmute.edu.vn.healthtracking.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import hcmute.edu.vn.healthtracking.R;
import hcmute.edu.vn.healthtracking.activities.ScheduleAddTask;
import hcmute.edu.vn.healthtracking.adapters.ScheduleAdapter;
import hcmute.edu.vn.healthtracking.database.DatabaseHelper;
import hcmute.edu.vn.healthtracking.models.Task;

public class ScheduleFragment extends Fragment implements ScheduleAdapter.OnTaskActionListener {

    private RecyclerView recyclerView;
    private ScheduleAdapter scheduleAdapter;
    private DatabaseHelper databaseHelper;
    private List<Task> taskList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_schedule, container, false);

        recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        Button btnShowCalendar = view.findViewById(R.id.btnShowCalendar);
        btnShowCalendar.setOnClickListener(v -> showCalendarDialog());

        Button btnAdd = view.findViewById(R.id.buttonaddtask);
        btnAdd.setOnClickListener(v -> {
            // Mở ScheduleAddTask (Activity)
            Intent intent = new Intent(requireContext(), ScheduleAddTask.class);
            startActivity(intent);
        });

        databaseHelper = new DatabaseHelper(requireContext());
        loadTasks();

        return view;
    }

    private void loadTasks() {
        taskList = databaseHelper.getAllTasks();
        scheduleAdapter = new ScheduleAdapter(requireContext(), taskList, this);
        recyclerView.setAdapter(scheduleAdapter);
    }

    private void showCalendarDialog() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.activity_view_calender, null);
        bottomSheetDialog.setContentView(view);

        CalendarView calendarView = view.findViewById(R.id.calendarView);
        List<EventDay> taskEvents = getTaskEvents();
        if (!taskEvents.isEmpty()) calendarView.setEvents(taskEvents);

        calendarView.setOnDayClickListener(eventDay -> {
            Calendar selectedDate = eventDay.getCalendar();
            SimpleDateFormat sdf = new SimpleDateFormat("EEE dd MMM", Locale.US);
            String selectedDateString = sdf.format(selectedDate.getTime());

            List<Task> filteredTasks = databaseHelper.getTasksByDate(selectedDateString);
            if (filteredTasks.isEmpty()) {
                Toast.makeText(getContext(), "Không có task nào cho ngày này!", Toast.LENGTH_SHORT).show();
            } else {
                scheduleAdapter.updateTasks(filteredTasks);
            }

            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();
    }

    private List<EventDay> getTaskEvents() {
        List<Task> taskList = databaseHelper.getAllTasks();
        List<EventDay> taskEvents = new ArrayList<>();

        for (Task task : taskList) {
            try {
                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("EEE dd MMM yyyy", Locale.US);
                Date date = sdf.parse(task.getDate() + " " + Calendar.getInstance().get(Calendar.YEAR));
                if (date != null) {
                    calendar.setTime(date);
                    taskEvents.add(new EventDay(calendar, R.drawable.ic_dotcalender));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return taskEvents;
    }

    @Override
    public void onCompleteTask(Task task) {
        showCompleteDialog(task);
    }

    private void showCompleteDialog(Task task) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.activity_dialog_complete_task, null);
        bottomSheetDialog.setContentView(view);

        Button closeButton = view.findViewById(R.id.buttonclose);
        closeButton.setOnClickListener(v -> {
            databaseHelper.deleteTask(task.getId());
            taskList.remove(task);
            scheduleAdapter.notifyDataSetChanged();
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();
    }

    @Override
    public void onEditTask(Task task) {
        Intent intent = new Intent(requireContext(), ScheduleAddTask.class);
        intent.putExtra("taskId", task.getId());
        intent.putExtra("taskTitle", task.getTitle());
        intent.putExtra("taskDescription", task.getDescription());
        intent.putExtra("taskDate", task.getDate());
        intent.putExtra("taskTime", task.getTime());
        startActivity(intent);
    }

    @Override
    public void onDeleteTask(Task task) {
        databaseHelper.deleteTask(task.getId());
        taskList.remove(task);
        scheduleAdapter.notifyDataSetChanged();
        Toast.makeText(getContext(), "Task đã được xoá", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTasks(); // Tự động reload khi quay lại Fragment
    }
}
