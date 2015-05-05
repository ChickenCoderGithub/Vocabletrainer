package com.rubengees.vocables.dialog;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.rubengees.vocables.R;
import com.rubengees.vocables.core.Core;
import com.rubengees.vocables.data.VocableManager;
import com.rubengees.vocables.pojo.Meaning;
import com.rubengees.vocables.pojo.Unit;
import com.rubengees.vocables.pojo.Vocable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ruben on 04.05.2015.
 */
public class VocableDialog extends DialogFragment {

    private Vocable vocable;
    private Unit unit;
    private VocableDialogCallback callback;

    private VocableManager manager;

    private LinearLayout meaningContainer1;
    private LinearLayout meaningContainer2;
    private ImageButton addMeaning1;
    private ImageButton addMeaning2;
    private EditText hint;
    private Spinner units;
    private EditText inputUnit;
    private ImageButton toggleUnit;

    public VocableDialog newInstance(int unitId, Vocable vocable) {
        VocableDialog dialog = new VocableDialog();
        Bundle bundle = new Bundle();

        bundle.putInt("unit_id", unitId);
        bundle.putParcelable("vocable", vocable);
        dialog.setArguments(bundle);

        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        manager = Core.getInstance(getActivity()).getVocableManager();

        if (getArguments() != null) {
            vocable = getArguments().getParcelable("vocable");
            unit = manager.getUnit(getArguments().getInt("unit_id"));
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity());

        builder.title("Modify Vocable").customView(inflateView(), true).positiveText("Save").negativeText("Cancel").callback(new MaterialDialog.ButtonCallback() {
            @Override
            public void onPositive(MaterialDialog dialog) {
                super.onPositive(dialog);

                if (processInput()) {
                    dialog.dismiss();
                }
            }

            @Override
            public void onNegative(MaterialDialog dialog) {
                super.onNegative(dialog);

                dialog.dismiss();
            }
        }).autoDismiss(false);

        processUnit();
        processVocable();
        setupButtons();

        return builder.build();
    }

    private boolean processInput() {
        List<String> firstMeanings = new ArrayList<>();
        List<String> secondMeanings = new ArrayList<>();
        Meaning firstMeaning = null;
        Meaning secondMeaning = null;
        String hint = null;
        String unitTitle;
        Unit unit = null;

        for (int i = 0; i < meaningContainer1.getChildCount(); i++) {
            EditText current = (EditText) meaningContainer1.getChildAt(i);
            String currentText = current.getText().toString().trim();

            if (!currentText.isEmpty()) {
                firstMeanings.add(currentText);
            }
        }

        for (int i = 0; i < meaningContainer2.getChildCount(); i++) {
            EditText current = (EditText) meaningContainer2.getChildAt(i);
            String currentText = current.getText().toString().trim();

            if (!currentText.isEmpty()) {
                secondMeanings.add(currentText);
            }
        }

        if (firstMeanings.isEmpty() || secondMeanings.isEmpty()) {
            Toast.makeText(getActivity(), "You have to give at least one Meaning", Toast.LENGTH_SHORT).show();

            return false;
        } else {
            firstMeaning = new Meaning(firstMeanings);
            secondMeaning = new Meaning(secondMeanings);
        }

        String hintText = this.hint.getText().toString().trim();

        if (!hintText.isEmpty()) {
            hint = hintText;
        }

        if (inputUnit.getVisibility() == View.VISIBLE) {
            unitTitle = inputUnit.getText().toString().trim();

            if (!unitTitle.isEmpty()) {
                unit = new Unit();
                unit.setTitle(unitTitle);
                unit.setLastModificationTime(System.currentTimeMillis());
            } else {
                Toast.makeText(getActivity(), "You have to name a unit", Toast.LENGTH_SHORT).show();

                return false;
            }
        } else {
            //TODO get unit from spinner
        }

        if (vocable == null) {
            Vocable current = new Vocable(firstMeaning, secondMeaning, hint, System.currentTimeMillis());

            if (callback != null) {
                callback.onVocableAdded(unit, current);
            }

            cleanUp(unit);

            return false;
        } else {
            vocable.setFirstMeaning(firstMeaning);
            vocable.setSecondMeaning(secondMeaning);
            vocable.setLastModificationTime(System.currentTimeMillis());
            vocable.setHint(hint);

            if (callback != null) {
                callback.onVocableChanged(unit, this.unit, vocable);
            }

            return true;
        }
    }

    private void cleanUp(Unit unit) {
        for (int i = 1; i < meaningContainer1.getChildCount(); i++) {
            meaningContainer1.removeViewAt(i);
        }

        for (int i = 1; i < meaningContainer2.getChildCount(); i++) {
            meaningContainer2.removeViewAt(i);
        }

        //TODO set unit
    }

    private void setupButtons() {
        addMeaning1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                meaningContainer1.addView(generateInput(null, "Meaning in your language"));
            }
        });

        addMeaning2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                meaningContainer1.addView(generateInput(null, "Meaning in foreign language"));
            }
        });

        toggleUnit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (units.getVisibility() == View.VISIBLE) {
                    setShowUnitInput(true);
                } else {
                    setShowUnitInput(false);
                }
            }
        });
    }

    private void setShowUnitInput(boolean show) {
        if (show) {
            units.setVisibility(View.GONE);
            toggleUnit.setImageResource(R.drawable.btn_hide);
        } else {
            units.setVisibility(View.VISIBLE);
            toggleUnit.setImageResource(R.drawable.btn_expand);
        }
    }

    private void processUnit() {
        //TODO populate Spinner

    }

    private void processVocable() {
        if (vocable != null) {
            for (String s : vocable.getFirstMeaning()) {
                meaningContainer1.addView(generateInput(s, "Meaning in your language"));
            }

            for (String s : vocable.getSecondMeaning()) {
                meaningContainer2.addView(generateInput(s, "Meaning in foreign language"));
            }
        } else {
            meaningContainer1.addView(generateInput(null, "Meaning in your language"));
            meaningContainer2.addView(generateInput(null, "Meaning in foreign language"));
        }

        this.hint.setText(vocable.getHint());

        //TODO set unit
    }

    private View inflateView() {
        View content = View.inflate(getActivity(), R.layout.dialog_vocable, null);

        meaningContainer1 = (LinearLayout) content.findViewById(R.id.dialog_vocable_meanings1_container);
        meaningContainer2 = (LinearLayout) content.findViewById(R.id.dialog_vocable_meanings2_container);
        addMeaning1 = (ImageButton) content.findViewById(R.id.dialog_vocable_meanings1_add);
        addMeaning2 = (ImageButton) content.findViewById(R.id.dialog_vocable_meanings2_add);
        hint = (EditText) content.findViewById(R.id.dialog_vocable_hint);
        units = (Spinner) content.findViewById(R.id.dialog_vocable_units);
        inputUnit = (EditText) content.findViewById(R.id.dialog_vocable_unit_input);
        toggleUnit = (ImageButton) content.findViewById(R.id.dialog_vocable_toggle_unit_input);

        return content;
    }

    private EditText generateInput(String text, String hint) {
        EditText result = (EditText) View.inflate(getActivity(), R.layout.dialog_vocable_input, null);

        result.setText(text);
        result.setHint(hint);
        result.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

        return result;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    public void setCallback(VocableDialogCallback callback) {
        this.callback = callback;
    }

    public interface VocableDialogCallback {
        void onVocableAdded(Unit unit, Vocable vocable);

        void onVocableChanged(Unit newUnit, Unit oldUnit, Vocable vocable);
    }
}
