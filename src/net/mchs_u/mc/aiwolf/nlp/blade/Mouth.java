package net.mchs_u.mc.aiwolf.nlp.blade;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.aiwolf.client.lib.Content;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Talk;
import org.aiwolf.common.net.GameInfo;

import net.mchs_u.mc.aiwolf.dokin.Estimate;
import net.mchs_u.mc.aiwolf.nlp.common.Transrater;

public class Mouth {
	private Set<String> talkedSet = null;
	
	private Agent todayVotedTarget = null;
	
	private Estimate estimate = null;
		
	public void initialize(Estimate estimate) {
		this.estimate = estimate;
		talkedSet = new HashSet<>();
	}
	
	public void dayStart() {
		todayVotedTarget = null;
	}

	public String toNaturalLanguageForTalk(GameInfo gameInfo, String protocol, Collection<String> answers) {
		if(!Content.validate(protocol)) {
			System.err.println("Mouth: 内部エージェントがプロトコル以外を喋ってる -> " + protocol);
			return Talk.SKIP;
		}
		Content content = new Content(protocol);

		if(gameInfo.getDay() == 0) { //　0日目は特殊
			if(!talkedSet.contains("0日目発言")){
				talkedSet.add("0日目発言");
				switch ((int)(Math.random() * 6)) {
				case 0: return "よろしくね。";
				case 1: return "こんにちは。";
				case 2: return "おはよう！";
				case 3: return "おはようございます。";
				case 4: return "頑張ります!";
				case 5: return "死なないように頑張ります。";
				}
			}
			return Talk.OVER;
		}

		switch (content.getTopic()) {
		case OVER:
			return Talk.OVER;
		case SKIP:
			return skipTalk(gameInfo, answers);
		case COMINGOUT:
			if(!content.getTarget().equals(gameInfo.getAgent()))
				return Talk.SKIP;
			if(content.getRole() == Role.WEREWOLF)
				return "わおーん、僕は人狼だよ。";
			return "僕は" + Transrater.roleToString(content.getRole()) + "だよ。";
		case DIVINED:
			String r = Transrater.speciesToString(content.getResult());
			switch ((int)(Math.random() * 5)) {
			case 0: return content.getTarget() + "さんの占い結果は、" + r + "だったよ。";
			case 1: return content.getTarget() + "さんの占いの結果は、" + r + "だったよ。";
			case 2: return content.getTarget() + "さんを占ったら、" + r + "だったよ。";
			case 3: return content.getTarget() + "さんを占った結果は、" + r + "だったよ。";
			case 4: return "昨日の占い結果だよ、" + content.getTarget() + "さんは" + r + "だったよ。";
			}
		case IDENTIFIED:
			return content.getTarget() + "さんの霊能結果は、" + Transrater.speciesToString(content.getResult()) + "だったよ。";
		case OPERATOR:
			Content c = content.getContentList().get(0);
			if(c.getTopic() != Topic.VOTE)
				return Talk.SKIP;
			return c.getTarget() + "さんに投票してね。";
		case VOTE:
			todayVotedTarget = content.getTarget();
			switch ((int)(Math.random() * 2)) {
			case 0: return content.getTarget() + "さんに投票するよ。";
			case 1: return content.getTarget() + "さんに投票しようかな。";
			}
		default:
			return Talk.SKIP;
		}
	}

	private String skipTalk(GameInfo gameInfo, Collection<String> answers) {
		if(estimate.isPowerPlay()) { // PPモード 
			if(!talkedSet.contains("パワープレイ反応")){
				talkedSet.add("パワープレイ反応");
				if(gameInfo.getRole() == Role.WEREWOLF) { // 人狼
					return "食べちゃうぞー！";
				} else if(gameInfo.getRole() == Role.POSSESSED) { // 狂人
					return "うひゃひゃひゃひゃひゃひゃひゃ！";
				} else { // 村人チーム
					return "え！　助けて！";
				}
			}
			return Talk.SKIP;
		}

		// 共通反応
		if(gameInfo.getLastDeadAgentList().size() > 0 && gameInfo.getDay() == 2) { // 2日目で襲撃死した人がいる場合
			if(!talkedSet.contains("襲撃反応")){
				talkedSet.add("襲撃反応");
				switch ((int)(Math.random() * 5)) {
				case 0: return "本当に襲われるなんて……。";
				case 1: return gameInfo.getLastDeadAgentList().get(0) + "さん……。";
				case 2: return "死んじゃった……。";
				}
			}
		}

		if(estimate.getCoMap().get(gameInfo.getAgent()) == Role.SEER) { // 自分が占い師COしてるとき
			if(estimate.getCoSet(Role.SEER).size() == 2) { //二人COしているとき
				if(!talkedSet.contains("対抗占い師反応")){
					talkedSet.add("対抗占い師反応");
					Set<Agent> coSeers = estimate.getCoSet(Role.SEER);
					coSeers.remove(gameInfo.getAgent());
					Agent t = (Agent)coSeers.toArray()[0];
					
					switch ((int)(Math.random() * 5)) {
					case 0: return t + "さんは嘘をついています！";
					case 1: return t + "さんは嘘つきです！";
					case 2: return ">>" + t + " " + t + "さん、あなたが人狼なんですか！？";
					}
				}
			}
		}
		
		// COしてない人
		if(estimate.getCoSet(Role.SEER).size() == 2) { //二人COしているとき
			if(!talkedSet.contains("二人占い師反応")){
				talkedSet.add("二人占い師反応");
				switch ((int)(Math.random() * 5)) {
				case 0: return "どっちが本当の占い師なんだろう……。";
				}
			}
		}
		
		for(String answer: answers) { //Earから渡されたAnswer
			if(!talkedSet.contains("answer:" + answer)){
				talkedSet.add("answer:" + answer);
				if(todayVotedTarget != null) {
					if(answer.startsWith(">>" + todayVotedTarget + " "))
						return answer.replace("#さん", "あなた");
					else
						return answer.replace("#", todayVotedTarget.toString());
				}
			}
		}
		
		return Talk.SKIP;
	}

	public String toNaturalLanguageForWhisper(GameInfo gameInfo, String protocol) {		
		if(!Content.validate(protocol)) {
			System.err.println("Mouth: 内部エージェントがプロトコル以外を喋ってる -> " + protocol);
			return Talk.SKIP;
		}
		Content content = new Content(protocol);

		switch (content.getTopic()) {
		case OVER:
			return Talk.OVER;
		case SKIP:
			return Talk.SKIP;
		case COMINGOUT:
			if(content.getTarget().equals(gameInfo.getAgent()) && content.getRole() == Role.VILLAGER)
				return "僕は潜伏するよ。";
			return Talk.SKIP;
		case ATTACK:
			return content.getTarget() + "を襲撃するよ。"; 
		default:
			return Talk.SKIP;
		}
	}
}
