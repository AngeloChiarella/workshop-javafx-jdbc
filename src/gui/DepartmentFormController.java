package gui;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import db.DbException;
import gui.listeners.DataChangeListener;
import gui.util.Alerts;
import gui.util.Constraints;
import gui.util.Utils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import model.entities.Department;
import model.exceptions.ValidationException;
import model.services.DepartmentService;

public class DepartmentFormController implements Initializable
{
	private Department entity;

	private DepartmentService service;

//	lista que permite outros objetos se inscreverem na lista e receberem eventos
	private List<DataChangeListener> dataChangeListeners = new ArrayList<>();

	@FXML
	private TextField txtId;

	@FXML
	private TextField txtName;

	@FXML
	private Label labelErrorName;

	@FXML
	private Button btSave;

	@FXML
	private Button btCancel;

	public void setDepartment(Department entity)
	{
		this.entity = entity;
	}

	public void setDepartmentService(DepartmentService service)
	{
		this.service = service;
	}

	public void subscribeDataChangeListener(DataChangeListener listener) // inscrever o listener na lista
	{
		dataChangeListeners.add(listener);
	}

	@FXML
	public void onBtSaveAction(ActionEvent event)
	{
		if (entity == null)
		{
			throw new IllegalStateException("Entity was null");
		}
		if (service == null)
		{
			throw new IllegalStateException("Service was null");
		}
		try
		{
			entity = getFormData();
			service.saveOrUpdate(entity);// Salva as alterações que veio do getFormData
			notifyDataChangeListeners();
			Utils.currentStage(event).close();// Pega a referencia da atual janela e fecha
		}
		catch (ValidationException e)
		{
			setErrorsMessages(e.getErrors());
		}
		catch (DbException e)
		{
			Alerts.showAlert("Error saving object", null, e.getMessage(), AlertType.ERROR);
		}
	}

	private void notifyDataChangeListeners() // executar onDataChanged em cada um dos Listener
	{
		for (DataChangeListener listener : dataChangeListeners)
		{
			listener.onDataChanged();
		}
	}

	private Department getFormData()// getFormData - pega o valor do label e instanciar um Department
	{
		Department obj = new Department();
		ValidationException exception = new ValidationException("Validation error");

		obj.setId(Utils.tryParseToInt(txtId.getText())); // Utils.tryParseToInt converte pra int ou null

		if (txtName.getText() == null || txtName.getText().trim().equals("")) // trim().equals - verifica espaço no fim
		{
			exception.addError("name", "Field can't be empty");
		}

		obj.setName(txtName.getText());

		if (exception.getErrors().size() > 0)
		{
			throw exception;
		}

		return obj;
	}

	@FXML
	public void onBtCancelAction(ActionEvent event)
	{
		Utils.currentStage(event).close();
	}

	@Override
	public void initialize(URL url, ResourceBundle rb)
	{
		initializeNodes();
	}

	public void initializeNodes()
	{
		Constraints.setTextFieldInteger(txtId);// Só aceita números inteiros
		Constraints.setTextFieldMaxLength(txtName, 30);// Máximo 30 caracteres
	}

	public void updateFormData()// Passar na label os dados do objeto do tipo entity
	{
		if (entity == null)// verificação, programação defensiva
		{
			throw new IllegalStateException("Entity was null");
		}
		txtId.setText(String.valueOf(entity.getId()));// String.valueOf - converter int para string
		txtName.setText(entity.getName());
	}

	private void setErrorsMessages(Map<String, String> errors)
	{
		Set<String> fields = errors.keySet(); // Percorrer o Map para preencher a label com o texto

		if (fields.contains("name")) // testando se tem chave name no conjunto de erros
		{
			labelErrorName.setText(errors.get("name"));
		}

	}
}