package net.mchs_u.mc.aiwolf.nlp.starter;

import java.io.IOException;
import java.net.SocketTimeoutException;

public class Main {
	public static void main(String[] args) throws InstantiationException, IllegalAccessException, SocketTimeoutException, IOException {
		int gameNum = 1;
		
		String type = null;
		type = "ローカル*5";
		//type = "ローカル*4 + 人間*1";
		//type = "大会5人接続";
		//type = "大会1人接続";
				
		switch (type) {
		case "ローカル*5":
			Starter.startServer(10000, gameNum, 300000);
			for(int i = 0; i < 5; i++)
				Starter.startAIClient("localhost", 10000);
			break;
		case "ローカル*4 + 人間*1":
			Starter.startServer(10000, gameNum, 300000);
			for(int i = 0; i < 4; i++)
				Starter.startAIClient("localhost", 10000);
			Starter.startHumanClient("localhost", 10000);
			break;
		case "大会5人接続":
			for(int i = 0; i < 4; i++)
				Starter.startAIClient("kachako.org", 10001);
			break;
		case "大会1人接続":
			Starter.startAIClient("kachako.org", 10001);
			break;
		}
		
	}

}
