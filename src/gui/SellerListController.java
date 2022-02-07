package gui;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import application.Main;
import db.DbIntegrityException;
import gui.listeners.DataChangeListener;
import gui.util.Alerts;
import gui.util.Utils;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.entities.Seller;
import model.services.DepartmentService;
import model.services.SellerService;

//************************ CUSTOMIZANDO MENU
public class SellerListController implements Initializable, DataChangeListener
{
	private SellerService service;

	@FXML
	private TableView<Seller> tableViewSeller;

	@FXML
	private TableColumn<Seller, Integer> tableColumnId;

	@FXML
	private TableColumn<Seller, String> tableColumnName;
	
	@FXML
	private TableColumn<Seller, String> tableColumnEmail;

	@FXML
	private TableColumn<Seller, Date> tableColumnBirthDate;
	
	@FXML
	private TableColumn<Seller, Double> tableColumnBaseSalary;

	@FXML
	private TableColumn<Seller, Seller> tableColumnEDIT;

	@FXML
	private TableColumn<Seller, Seller> tableColumnREMOVE;

	@FXML
	private Button btNew;

	@FXML
	private ObservableList<Seller> obsList;// Recebe os departamentos

	@FXML
	public void onBtNewAction(ActionEvent event)// referencia para controle q recebeu evento
	{
		Stage parentStage = Utils.currentStage(event);
		Seller obj = new Seller();
		createDialogForm(obj, "/gui/SellerForm.fxml", parentStage);
	}

	// Método set para injetar dependencia no service, sem fazer acomplamento forte
	public void setSellerService(SellerService service)
	{// Principio solid - inversão de controle
		this.service = service;
	}

	@Override
	public void initialize(URL url, ResourceBundle rb)
	{
		initializeNodes();// iniciar componente na tela
	}

	private void initializeNodes()// Padrão no JavaFX para inicar o comportamento das colunas
	{
		tableColumnId.setCellValueFactory(new PropertyValueFactory<>("id"));
		tableColumnName.setCellValueFactory(new PropertyValueFactory<>("name"));
		tableColumnEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
		tableColumnBirthDate.setCellValueFactory(new PropertyValueFactory<>("birthDate"));
		Utils.formatTableColumnDate(tableColumnBirthDate, "dd/MM/yyyy");
		tableColumnBaseSalary.setCellValueFactory(new PropertyValueFactory<>("baseSalary"));
		Utils.formatTableColumnDouble(tableColumnBaseSalary, 2);

		// Referencia para Stage para ajustar o tamanho da tabela na Janela
		Stage stage = (Stage) Main.getMainScene().getWindow();// Main.getMainScene - acessar a cena | getWindow - pega ref da janela (Super
																// classe do Stage)
		tableViewSeller.prefHeightProperty().bind(stage.heightProperty()); // Comando para a tableView acompanhar a janela
	}

	public void updateTableView()
	{
		if (service == null)// Injeção de dependencia está manual, por isso o tratamento
		{
			throw new IllegalStateException("Service was null");
		}
		List<Seller> list = service.findAll();// Recuperar os Deps do Serviço
		obsList = FXCollections.observableArrayList(list);// Instancia a observableList passando como argumento list pegando dados
															// originais
		tableViewSeller.setItems(obsList);// carregar os itens na tableView
		initEditButtons(); // Acrescenta um novo botão com texto "edit" para cada linha da tabela
		initiRemoveButtons(); // REMOVER
	}

	private void createDialogForm(Seller obj, String absoluteName, Stage parentStage)
	{
		try
		{
			FXMLLoader loader = new FXMLLoader(getClass().getResource(absoluteName));
			Pane pane = loader.load();// carregar a view

			SellerFormController controller = loader.getController();
			controller.setSeller(obj);
			controller.setServices(new SellerService(), new DepartmentService());
			controller.loadAssociatedObjects();
			controller.subscribeDataChangeListener(this);
			controller.updateFormData();

//			Carregar uma modal na frente de uma janela existente - um palco na frente do outro
			Stage dialogStage = new Stage();
			dialogStage.setTitle("Enter seller data");
			dialogStage.setScene(new Scene(pane));// criar nova cena
			dialogStage.setResizable(false);// setResizable - define se a janela pode ou não, ser redimencionada
			dialogStage.initOwner(parentStage);// initOwner - pai da janela
			dialogStage.initModality(Modality.WINDOW_MODAL);// dizer se é modal ou não | Modality.WINDOW_MODAL - trava, impedindo de acessar
															// a janela anterior
			dialogStage.showAndWait();// carregar a janela do formulario pra prencher campos do dep.

		}
		catch (IOException e)
		{
			e.printStackTrace();
			Alerts.showAlert("IOException", "Error loading view", e.getMessage(), AlertType.ERROR);
		}
	}

	@Override
	public void onDataChanged()// Salva os dados quando forem alterados
	{
		updateTableView();
	}

	private void initEditButtons()
	{
		tableColumnEDIT.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
		tableColumnEDIT.setCellFactory(param -> new TableCell<Seller, Seller>()
		{
			private final Button button = new Button("edit");

			@Override
			protected void updateItem(Seller obj, boolean empty)
			{
				super.updateItem(obj, empty);
				if (obj == null)
				{
					setGraphic(null);
					return;
				}
				setGraphic(button);// createDialogForm - abre formulário de edição
				button.setOnAction(event -> createDialogForm(obj, "/gui/SellerForm.fxml", Utils.currentStage(event)));
			}
		});
	}

	private void initiRemoveButtons()
	{
		tableColumnREMOVE.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>((param.getValue())));
		tableColumnREMOVE.setCellFactory(param -> new TableCell<Seller, Seller>()
		{
			private final Button button = new Button("remove");

			@Override
			protected void updateItem(Seller obj, boolean empty)
			{
				super.updateItem(obj, empty);

				if (obj == null)
				{
					setGraphic(null);
					return;
				}
				setGraphic(button);
				button.setOnAction(event -> removeEntity(obj));
			}
		});
	}

	private void removeEntity(Seller obj)
	{
		Optional<ButtonType> result = Alerts.showConfirmation("Confirmation", "Are you sure to delete?");
		if (result.get() == ButtonType.OK)
		{
			if (service == null)
			{
				throw new IllegalStateException("Service was nulll");
			}
			try
			{
				service.remove(obj);
				updateTableView();
			}
			catch (DbIntegrityException e)
			{
				Alerts.showAlert("Error remove object", null, e.getMessage(), AlertType.ERROR);
			}
		}
	}

}
