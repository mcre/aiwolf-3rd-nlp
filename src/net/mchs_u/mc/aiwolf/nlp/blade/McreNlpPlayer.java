package net.mchs_u.mc.aiwolf.nlp.blade;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Player;
import org.aiwolf.common.data.Talk;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

import net.mchs_u.mc.aiwolf.dokin.McrePlayer;

public class McreNlpPlayer implements Player {
	private McrePlayer player = null;
	
	private GameInfo gameInfo = null;
	private Mouth mouth;
	private Ear ear;
	//private int listHead; // トークをどこまでprint/処理したかの管理

	public McreNlpPlayer() {
		player = new net.mchs_u.mc.aiwolf.dokin.McrePlayer();
		ear = new Ear(player);
		mouth = new Mouth(player);
	}
	
	public void update(GameInfo gameInfo) {
		try {
			this.gameInfo = gameInfo;
			GameInfo prGameInfo = new GameInfoTranslater(gameInfo, ear);
			player.update(prGameInfo);
		} catch(Exception e) {
			System.err.println("エラー発生, SKIP送信(update)");
			e.printStackTrace();
		}
	}
	
	public String talk() {
		try {
			String pr = Talk.SKIP;
			if(gameInfo.getDay() > 0 && gameInfo.getTalkList().size() > 0)
				pr = player.talk(); // 0日目とその日１回目のtalkはプロトコル版のtalkを呼ばない
			String nl = mouth.toNaturalLanguageForTalk(gameInfo, pr, ear.getAnswers());
			System.out.println("　●talk: " + gameInfo.getAgent() + " " + getName() + "\t" + nl + " ( <- " + pr + " ) ");
			return nl;
		} catch(Exception e) {
			System.err.println("エラー発生, SKIP送信(talk)");
			e.printStackTrace();
		}
		return Talk.SKIP;
	}
	
	public String whisper() {
		String pr = player.whisper();
		String nl = mouth.toNaturalLanguageForWhisper(gameInfo, pr);
		System.out.println("　●whis: " + gameInfo.getAgent() + " " + getName() + "\t" + nl + " ( <- " + pr + " ) ");
		return nl;
	}
	
	public void initialize(GameInfo gameInfo, GameSetting gameSetting) {
		player.initialize(gameInfo, gameSetting);
		ear.initialize();
		mouth.initialize(gameInfo);
		
		System.out.println("McreNlpPlayer.initialize, " + gameInfo.getAgent() + ", " + gameInfo.getRole());
	}
	
	public void dayStart() {
		//listHead = 0;
		player.dayStart();
		mouth.dayStart();
		ear.dayStart();
	}

	public Agent attack() {
		return player.attack();
	}

	public Agent divine() {
		return player.divine();
	}

	public void finish() {
		ear.save();
		player.finish();
	}

	public String getName() {
		if(player == null)
			return "m_cre";
		return player.getName();
	}

	public Agent guard() {
		return player.guard();
	}

	public Agent vote() {
		return player.vote();
	}

}
