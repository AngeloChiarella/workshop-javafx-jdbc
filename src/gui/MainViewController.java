package gui;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;

import application.Main;
import gui.util.Alerts;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import model.services.DepartmentService;
import model.services.SellerService;

//************************ CUSTOMIZANDO MENU
public class MainViewController implements Initializable
{
	@FXML
	private MenuItem menuItemSeller;

	@FXML
	private MenuItem menuItemDepartment;

	@FXML
	private MenuItem menuItemAbout;

	@FXML
	public void onMenuItemSellerAction()
	{
		loadView("/gui/SellerList.fxml", (SellerListController controller) ->
		{
			controller.setSellerService(new SellerService());
			controller.updateTableView();
		});
	}

	@FXML
	public void onMenuItemDepartmentAction()
	{ // Exp Lambda para eliminar o loadView2
		loadView("/gui/DepartmentList.fxml", (DepartmentListController controller) ->
		{
			controller.setDepartmentService(new DepartmentService());
			controller.updateTableView();
		});
	}

	@FXML
	public void onMenuItemAboutAction()
	{
		loadView("/gui/About.fxml", x -> {});
	}

	@Override
	public void initialize(URL url, ResourceBundle rb)
	{

	}

//	synchronized - Garante que o processo não será interrompido | Consumer - Declarar lambda T generics
	private synchronized <T> void loadView(String absoluteName, Consumer<T> initializingAction)
	{
//		Instanciar 'FXMLLoader' para carregar uma tela
		try
		{
			FXMLLoader loader = new FXMLLoader(getClass().getResource(absoluteName));
			VBox newVBox = loader.load();

//Referencia pra cena Scene para acrescentar nos filhos do VBOX do MainView, os filhos do VBOX da janela About - Tudo isso em 'arquivo'.fxml
			Scene mainScene = Main.getMainScene();
//	Pegar o primeiro elemento da view principal 'ScrollPane' | getContent - referencia pro que está dentro do ScrollPane 
			VBox mainVBox = (VBox) ((ScrollPane) mainScene.getRoot()).getContent();
//	Excluir filhos do MainView e incluir os filhos do About no MainView
			Node mainMenu = mainVBox.getChildren().get(0); // referencia para o menu recebendo MainVbox na posição 0
			mainVBox.getChildren().clear();// Limpar os filho do MainVBox
			mainVBox.getChildren().add(mainMenu);// adicionar o MainMenu
			mainVBox.getChildren().addAll(newVBox.getChildren());// adicionar os filhos

// Essas duas linhas abaixo executa a função que foi passada como argumento em lambda no loadView para não precisar da loadView2 ou mais
			T controller = loader.getController();// getController - retorna controlador do tipo que foi chamado no loadView
			initializingAction.accept(controller);// initializingAction.accept - executar controller
		}
		catch (IOException e)
		{
			Alerts.showAlert("IO Exception", "Error loading view", e.getMessage(), AlertType.ERROR);
		}
	}

//	private synchronized void loadView2(String absoluteName)
//	{
////		Instanciar 'FXMLLoader' para carregar uma tela
//		try
//		{
//			FXMLLoader loader = new FXMLLoader(getClass().getResource(absoluteName));
//			VBox newVBox = loader.load();
//
////Referencia pra cena Scene para acrescentar nos filhos do VBOX do MainView, os filhos do VBOX da janela About - Tudo isso em 'arquivo'.fxml
//			Scene mainScene = Main.getMainScene();
////			Pegar o primeiro elemento da view principal 'ScrollPane' | getContent - referencia pro que está dentro do ScrollPane 
//			VBox mainVBox = (VBox) ((ScrollPane) mainScene.getRoot()).getContent();
////			Excluir filhos do MainView e incluir os filhos do About no MainView
//			Node mainMenu = mainVBox.getChildren().get(0); // referencia para o menu recebendo MainVbox na posição 0
//			mainVBox.getChildren().clear();// Limpar os filho do MainVBox
//			mainVBox.getChildren().add(mainMenu);// adicionar o MainMenu
//			mainVBox.getChildren().addAll(newVBox.getChildren());// adicionar os filhos
////			Carrega a view e acessa o controller
//			DepartmentListController controller = loader.getController();//loader - carrega a view
//			controller.setDepartmentService(new DepartmentService());//Injetando a dependencia
//			controller.updateTableView();//atualiza os dados na tela	
//		}
//		catch (IOException e)
//		{
//			Alerts.showAlert("IO Exception", "Error loading view", e.getMessage(), AlertType.ERROR);
//		}
//	}

}
