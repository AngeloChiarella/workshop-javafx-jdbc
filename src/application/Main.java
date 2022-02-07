package application;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.stage.Stage;

public class Main extends Application
{
	private static Scene mainScene;

	@Override
	public void start(Stage primaryStage)
	{
		try
		{
//			Agora vamos Instanciar o m�todo FXMLLoader (N�o chama mais o m�todo est�tico)
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/MainView.fxml"));
			ScrollPane scrollPane = loader.load();// loader.load - Carrega a view

			scrollPane.setFitToHeight(true); // Ajustar Painel Scrol na Scene(altura)
			scrollPane.setFitToWidth(true); // Ajustar Painel Scrol na Scene(largura)

			mainScene = new Scene(scrollPane);// new Scene - Cria um obj tipo Principal Scene com argumento 'scrollPane'
			primaryStage.setScene(mainScene);// setScene - Setar usando atributo do m�todo start passando a cena craiada
												// instanciada 'primaryStage.setScene(mainScene)'
			primaryStage.setTitle("Sample JavaFX application");// Definir t�tulo para o palco
			primaryStage.show();// Mostrar o Palco
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public static Scene getMainScene()
	{
		return mainScene;
	}

	public static void main(String[] args)
	{
		launch(args);
	}
}
