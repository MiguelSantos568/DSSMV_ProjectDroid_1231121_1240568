
package com.example.condosched.auth;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.condosched.R;
import com.example.condosched.network.AuthService;
import com.example.condosched.network.CondominiumService;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.TreeSet;

public class RegisterActivity extends Activity {

    private EditText nameInput, emailInput, passwordInput;
    private Button registerButton;

    private Spinner condominiumSpinner, buildingSpinner;
    private Spinner floorSpinner, unitSpinner;

    private View buildingLabel, floorLabel, unitLabel;

    private final AuthService authService = new AuthService();
    private final CondominiumService condominiumService =
            new CondominiumService();

    private JSONArray condominiums = new JSONArray();
    private JSONArray buildings = new JSONArray();
    private JSONArray units = new JSONArray();

    private final ArrayList<Integer> floors = new ArrayList<>();
    private final ArrayList<JSONObject> availableUnits = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        registerButton = findViewById(R.id.registerButton);

        condominiumSpinner = findViewById(R.id.condominiumSpinner);
        buildingSpinner = findViewById(R.id.buildingSpinner);
        floorSpinner = findViewById(R.id.floorSpinner);
        unitSpinner = findViewById(R.id.unitSpinner);

        buildingLabel = findViewById(R.id.buildingLabel);
        floorLabel = findViewById(R.id.floorLabel);
        unitLabel = findViewById(R.id.unitLabel);

        registerButton.setEnabled(false);
        registerButton.setOnClickListener(v -> register());

        carregarCondominios();
    }

    // Preenche um dropdown com uma lista de nomes
    private void preencherSpinner(Spinner spinner,
                                  ArrayList<String> nomes) {

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                nomes
        );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinner.setAdapter(adapter);
    }

    // Esconde os campos seguintes
    private void esconder(View label, Spinner spinner) {
        spinner.setOnItemSelectedListener(null);
        spinner.setAdapter(null);
        label.setVisibility(View.GONE);
        spinner.setVisibility(View.GONE);
    }

    // Carrega os condomínios através da API
    private void carregarCondominios() {

        condominiumService.getCondominiums(
                new CondominiumService.CondominiumCallback() {

                    @Override
                    public void onSuccess(JSONArray result) {

                        condominiums = result;

                        ArrayList<String> nomes = new ArrayList<>();
                        nomes.add("Seleciona o condomínio");

                        for (int i = 0; i < result.length(); i++) {
                            JSONObject condo = result.optJSONObject(i);

                            if (condo != null) {
                                nomes.add(condo.optString("name"));
                            }
                        }

                        preencherSpinner(condominiumSpinner, nomes);

                        condominiumSpinner.setOnItemSelectedListener(
                                new AdapterView.OnItemSelectedListener() {

                                    @Override
                                    public void onItemSelected(
                                            AdapterView<?> parent,
                                            View view, int position, long id) {

                                        carregarPredios(position);
                                    }

                                    @Override
                                    public void onNothingSelected(
                                            AdapterView<?> parent) {
                                    }
                                });
                    }

                    @Override
                    public void onError(String message) {
                        mostrar("Erro ao carregar condomínios: " + message);
                    }
                });
    }

    // Mostra os prédios do condomínio selecionado
    private void carregarPredios(int position) {

        esconder(buildingLabel, buildingSpinner);
        esconder(floorLabel, floorSpinner);
        esconder(unitLabel, unitSpinner);

        registerButton.setEnabled(false);

        if (position <= 0) return;

        JSONObject condo = condominiums.optJSONObject(position - 1);
        if (condo == null) return;

        buildings = condo.optJSONArray("buildings");
        if (buildings == null) return;

        ArrayList<String> nomes = new ArrayList<>();
        nomes.add("Seleciona o prédio");

        for (int i = 0; i < buildings.length(); i++) {
            JSONObject building = buildings.optJSONObject(i);

            if (building != null) {
                nomes.add(building.optString("name"));
            }
        }

        preencherSpinner(buildingSpinner, nomes);

        buildingLabel.setVisibility(View.VISIBLE);
        buildingSpinner.setVisibility(View.VISIBLE);

        buildingSpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent,
                            View view, int position, long id) {

                        carregarAndares(position);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
    }

    // Mostra os andares do prédio selecionado
    private void carregarAndares(int position) {

        esconder(floorLabel, floorSpinner);
        esconder(unitLabel, unitSpinner);

        registerButton.setEnabled(false);

        if (position <= 0) return;

        JSONObject building = buildings.optJSONObject(position - 1);
        if (building == null) return;

        units = building.optJSONArray("units");
        if (units == null) return;

        TreeSet<Integer> andares = new TreeSet<>();

        for (int i = 0; i < units.length(); i++) {
            JSONObject unit = units.optJSONObject(i);

            if (unit != null) {
                andares.add(unit.optInt("floor"));
            }
        }

        floors.clear();
        floors.addAll(andares);

        ArrayList<String> nomes = new ArrayList<>();
        nomes.add("Seleciona o andar");

        for (int andar : floors) {
            nomes.add("Andar " + andar);
        }

        preencherSpinner(floorSpinner, nomes);

        floorLabel.setVisibility(View.VISIBLE);
        floorSpinner.setVisibility(View.VISIBLE);

        floorSpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent,
                            View view, int position, long id) {

                        carregarPortas(position);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
    }

    // Mostra as portas do andar selecionado
    private void carregarPortas(int position) {

        esconder(unitLabel, unitSpinner);
        registerButton.setEnabled(false);
        availableUnits.clear();

        if (position <= 0 || position > floors.size()) return;

        int andar = floors.get(position - 1);

        ArrayList<String> portas = new ArrayList<>();
        portas.add("Seleciona a porta");

        for (int i = 0; i < units.length(); i++) {
            JSONObject unit = units.optJSONObject(i);

            if (unit != null && unit.optInt("floor") == andar) {
                availableUnits.add(unit);
                portas.add(unit.optString("unitNumber"));
            }
        }

        preencherSpinner(unitSpinner, portas);

        unitLabel.setVisibility(View.VISIBLE);
        unitSpinner.setVisibility(View.VISIBLE);

        unitSpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent,
                            View view, int position, long id) {

                        registerButton.setEnabled(
                                position > 0 &&
                                        position <= availableUnits.size()
                        );
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        registerButton.setEnabled(false);
                    }
                });
    }

    // Regista o utilizador no Supabase
    private void register() {

        String name = nameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            mostrar("Preenche todos os campos");
            return;
        }

        int position = unitSpinner.getSelectedItemPosition();

        if (position <= 0 || position > availableUnits.size()) {
            mostrar("Seleciona a tua habitação");
            return;
        }

        JSONObject selectedUnit = availableUnits.get(position - 1);
        long unitId = selectedUnit.optLong("id");

        registerButton.setEnabled(false);

        authService.register(email, password,
                new AuthService.LoginCallback() {

                    @Override
                    public void onSuccess() {
                        mostrar("Verifica o teu email para confirmar a conta!");
                        finish();
                    }

                    @Override
                    public void onError(String message) {
                        registerButton.setEnabled(true);
                        mostrar(message);
                    }
                });

        // O nome e o unitId serão guardados no perfil
        // quando implementarmos essa funcionalidade.
    }

    private void mostrar(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
