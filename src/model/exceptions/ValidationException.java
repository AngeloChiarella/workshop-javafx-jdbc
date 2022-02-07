package model.exceptions;

import java.util.HashMap;
import java.util.Map;

public class ValidationException extends RuntimeException
{
//	********** Exception personalizada que carrega coleção contendo todos erros possiveis
	private static final long serialVersionUID = 1L;

	private Map<String, String> errors = new HashMap<>();// Map - Coleção de pares (chave, valor)

	public ValidationException(String msg)
	{
		super(msg);
	}

	public Map<String, String> getErrors()
	{
		return errors;
	}

	public void addError(String fieldName, String errorMessage)
	{
		errors.put(fieldName, errorMessage);
	}
}
