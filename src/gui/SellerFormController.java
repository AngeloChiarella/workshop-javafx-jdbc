package gui;

import java.net.URL;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import db.DbException;
import gui.listeners.DataChangeListener;
import gui.util.Alerts;
import gui.util.Constraints;
import gui.util.Utils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.util.Callback;
import model.entities.Department;
import model.entities.Seller;
import model.exceptions.ValidationException;
import model.services.DepartmentService;
import model.services.SellerService;

public class SellerFormController implements Initializable
{
	private Seller entity;

	private SellerService service;

	private DepartmentService departmentService;

//	lista que permite outros objetos se inscreverem na lista e receberem eventos
	private List<DataChangeListener> dataChangeListeners = new ArrayList<>();

	@FXML
	private TextField txtId;

	@FXML
	private TextField txtName;

	@FXML
	private TextField txtEmail;

	@FXML
	private DatePicker dpBirthDate;

	@FXML
	private TextField txtBaseSalary;

	@FXML
	private ComboBox<Department> comboBoxDepartment;

	@FXML
	private Label labelErrorName;

	@FXML
	private Label labelErrorEmail;

	@FXML
	private Label labelErrorBirthDate;

	@FXML
	private Label labelErrorBaseSalary;

	@FXML
	private Button btSave;

	@FXML
	private Button btCancel;

	private ObservableList<Department> obsList;

	public void setSeller(Seller entity)
	{
		this.entity = entity;
	}

	public void setServices(SellerService service, DepartmentService departmentService)
	{
		this.service = service;
		this.departmentService = departmentService;
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

	private Seller getFormData()// getFormData - pega o valor do label e instanciar um Seller
	{
		Seller obj = new Seller();
		ValidationException exception = new ValidationException("Validation error");

		obj.setId(Utils.tryParseToInt(txtId.getText())); // Utils.tryParseToInt converte pra int ou null

		if (txtName.getText() == null || txtName.getText().trim().equals("")) // trim().equals - verifica espaço no fim
		{
			exception.addError("name", "Field can't be empty");
		}
		obj.setName(txtName.getText());

		if (txtEmail.getText() == null || txtEmail.getText().trim().equals("")) // trim().equals - verifica espaço no fim
		{
			exception.addError("email", "Field can't be empty");
		}
		obj.setEmail(txtEmail.getText());

		if (dpBirthDate.getValue() == null)
		{
			exception.addError("birthDate", "Field can't be empty");
		}
		else
		{
			Instant instant = Instant.from(dpBirthDate.getValue().atStartOfDay(ZoneId.systemDefault()));
			obj.setBirthDate(Date.from(instant));
		}
		if (txtBaseSalary.getText() == null || txtBaseSalary.getText().trim().equals("")) // trim().equals - verifica espaço no fim
		{
			exception.addError("baseSalary", "Field can't be empty");
		}
		obj.setBaseSalary(Utils.tryParseToDouble(txtBaseSalary.getText()));

		obj.setDepartment(comboBoxDepartment.getValue());
		
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
		Constraints.setTextFieldDouble(txtBaseSalary);
		Constraints.setTextFieldMaxLength(txtEmail, 60);
		Utils.formatDatePicker(dpBirthDate, "dd/MM/yyyy");

		initializeComboBoxDepartment();
	}

	public void updateFormData()// Passar na label os dados do objeto do tipo entity
	{
		if (entity == null)// verificação, programação defensiva
		{
			throw new IllegalStateException("Entity was null");
		}
		txtId.setText(String.valueOf(entity.getId()));// String.valueOf - converter int para string
		txtName.setText(entity.getName());
		txtEmail.setText(entity.getEmail());
		Locale.setDefault(Locale.US);
		txtBaseSalary.setText(String.format("%.2f", entity.getBaseSalary()));
		if (entity.getBirthDate() != null)
		{
// Setando a data | LocalDate - mostra data no formato local | ofInstant pega o formato do instante
			dpBirthDate.setValue(LocalDate.ofInstant(entity.getBirthDate().toInstant(), ZoneId.systemDefault()));
		}
		if (entity.getDepartment() == null)
		{
			comboBoxDepartment.getSelectionModel().selectFirst();
		}
		else
		{
			comboBoxDepartment.setValue(entity.getDepartment());
		}
	}

	public void loadAssociatedObjects()
	{
		if (departmentService == null)
		{
			throw new IllegalStateException("DepartmentService was null");
		}
		List<Department> list = departmentService.findAll();
		obsList = FXCollections.observableArrayList(list);
		comboBoxDepartment.setItems(obsList);
	}

	private void setErrorsMessages(Map<String, String> errors)
	{
		Set<String> fields = errors.keySet(); // Percorrer o Map para preencher a label com o texto

//		if (fields.contains("name")) // testando se tem chave name no conjunto de erros
//		{
//			labelErrorName.setText(errors.get("name"));
//		}else {
//			labelErrorName.setText("");
//		}
		labelErrorName.setText(fields.contains("name") ? errors.get("name") : "");
		
//		if (fields.contains("email")) // testando se tem chave name no conjunto de erros
//		{
//			labelErrorEmail.setText(errors.get("email"));
//		}
		labelErrorEmail.setText(fields.contains("email") ? errors.get("email") : "");
		
//		if (fields.contains("baseSalary")) // testando se tem chave name no conjunto de erros
//		{
//			labelErrorBaseSalary.setText(errors.get("baseSalary"));
//		}
		labelErrorBaseSalary.setText(fields.contains("baseSalary") ? errors.get("baseSalary") : "");
		
//		if (fields.contains("birthDate")) // testando se tem chave name no conjunto de erros
//		{
//			labelErrorBirthDate.setText(errors.get("birthDate"));
//		}
		labelErrorBirthDate.setText(fields.contains("birthDate") ? errors.get("birthDate") : "");
	}

	private void initializeComboBoxDepartment()
	{
		Callback<ListView<Department>, ListCell<Department>> factory = lv -> new ListCell<Department>()
		{
			@Override
			protected void updateItem(Department item, boolean empty)
			{
				super.updateItem(item, empty);
				setText(empty ? "" : item.getName());
			}
		};
		comboBoxDepartment.setCellFactory(factory);
		comboBoxDepartment.setButtonCell(factory.call(null));
	}
}