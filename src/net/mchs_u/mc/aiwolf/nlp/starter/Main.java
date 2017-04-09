package net.mchs_u.mc.aiwolf.nlp.starter;

import java.io.IOException;
import java.net.SocketTimeoutException;

public class Main {
	public static void main(String[] args) throws InstantiationException, IllegalAccessException, SocketTimeoutException, IOException  {
		String type = null;
		type = "ローカル自己対戦";
		//type = "大会5人接続";
		//type = "大会1人接続";
		
		String[] names1  = {"m_cre"};
		String[] names5A = {"m_cre",  "m_cre",  "m_cre",  "m_cre",  "m_cre"};
		String[] names5B = {"m_cre1", "m_cre2", "m_cre3", "m_cre4", "m_cre5"};
		
		switch (type) {
		case "ローカル自己対戦用":
			Starter.startServer(10000, 100, 60000);
			Starter.startClient("localhost", 10000, 5, names5B);
			break;
		case "大会5人接続":
			Starter.startClient("kanolab.net", 10000, 5, names5A);
			break;
		case "大会1人接続":
			Starter.startClient("kanolab.net", 10000, 1, names1);
			break;
		}
		
	}

}
