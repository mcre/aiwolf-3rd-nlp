package net.mchs_u.mc.aiwolf.nlp.blade;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.aiwolf.client.lib.Content;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Talk;
import org.aiwolf.common.net.GameInfo;

import net.mchs_u.mc.aiwolf.dokin.Estimate;
import net.mchs_u.mc.aiwolf.dokin.McrePlayer;
import net.mchs_u.mc.aiwolf.nlp.common.Transrater;

public class Mouth {
	private Set<String> talkedSet = null;
	private McrePlayer player = null;
	private Map<String, String> c = null; // character
	
	private boolean firstVoted = false;

	public Mouth(McrePlayer player) {
		this.player = player;
	}

	public void initialize(GameInfo gameInfo) {
		talkedSet = new HashSet<>();
		c = Character.getCharactorMap(gameInfo.getAgent().getAgentIdx());
		firstVoted = false;
	}

	public void dayStart() {
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
				return r("<こんにちは。>");
			}
			return Talk.OVER;
		}

		Agent t = content.getTarget();
		switch (content.getTopic()) {
		case OVER:
			return Talk.OVER;
		case SKIP:
			return skipTalk(gameInfo, answers);
		case COMINGOUT:
			if(!content.getTarget().equals(gameInfo.getAgent()))
				return Talk.SKIP;
			if(content.getRole() == Role.WEREWOLF)
				return r("わおーん、<僕>は人狼だ<よ>。");
			return r("<僕>は" + Transrater.roleToString(content.getRole()) + "だ<よ>。");
		case DIVINED:
			String r = Transrater.speciesToString(content.getResult());
			t = content.getTarget();
			switch ((int)(Math.random() * 5)) {
			case 0: return r(t + "<さん>の占い結果は、" + r + "だった<よ>。");
			case 1: return r(t + "<さん>の占いの結果は、" + r + "だった<よ>。");
			case 2: return r(t + "<さん>を占ったら、" + r + "だった<よ>。");
			case 3: return r(t + "<さん>を占った結果は、" + r + "だった<よ>。");
			case 4: return r("昨日の占い結果だ<よ>、" + t + "<さん>は" + r + "だった<よ>。");
			}
		case IDENTIFIED:
			return r(content.getTarget() + "<さん>の霊能結果は、" + Transrater.speciesToString(content.getResult()) + "だった<よ>。");
		case OPERATOR:
			Content c = content.getContentList().get(0);
			if(c.getTopic() != Topic.VOTE)
				return Talk.SKIP;
			return r(c.getTarget() + "<さん>に投票して<ね>。");
		case VOTE:
			// 1回目の投票宣言は何も情報がない中での宣言なのでスルーする
			if(firstVoted) {
				switch ((int)(Math.random() * 2)) {
				case 0: return r(t + "<さん>に投票する<よ>。");
				case 1: return r(t + "<さん>に投票しようかな。");
				}
			}
			firstVoted = true;
		default:
			return Talk.SKIP;
		}
	}

	private String skipTalk(GameInfo gameInfo, Collection<String> answers) {
		if(getEstimate().isPowerPlay()) { // PPモード 
			if(!talkedSet.contains("パワープレイ反応")){
				talkedSet.add("パワープレイ反応");
				if(gameInfo.getRole() == Role.WEREWOLF) { // 人狼
					return r("食べちゃう<よ>ー！");
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
				case 1: return r(gameInfo.getLastDeadAgentList().get(0) + "<さん>が死んだ……。");
				}
			}
		}

		if(getEstimate().getCoMap().get(gameInfo.getAgent()) == Role.SEER) { // 自分が占い師COしてるとき
			if(getEstimate().getCoSet(Role.SEER).size() == 2) { //二人COしているとき
				if(!talkedSet.contains("対抗占い師反応")){
					talkedSet.add("対抗占い師反応");
					Set<Agent> coSeers = getEstimate().getCoSet(Role.SEER);
					coSeers.remove(gameInfo.getAgent());
					Agent t = (Agent)coSeers.toArray()[0];

					switch ((int)(Math.random() * 5)) {
					case 0: return r(t + "<さん>は嘘をついて<います>！");
					case 1: return r(t + "<さん>は嘘つき<です>！");
					case 2: return r(">>" + t + " " + t + "<さん>、<あなた>が人狼<なのですか>！？");
					}
				}
			}
		}

		// COしてない人
		if(getEstimate().getCoSet(Role.SEER).size() == 2) { //二人COしているとき
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
				
				Agent voteTarget = player.getVoteTarget();
				if(voteTarget != null) {
					if(answer.startsWith(">>" + voteTarget + " "))
						return r(answer.replace("#<さん>", "<あなた>"));
					else
						return r(answer.replace("#", voteTarget.toString()));
				}
			}
		}

		return Talk.SKIP;
	}
	
	private String r(String s) { // replace
		String ret = s;
		for(String key: c.keySet())
			ret = ret.replace("<" + key + ">", c.get(key));
		return ret;
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
				return r("<僕>は潜伏する<よ>。");
			return Talk.SKIP;
		case ATTACK:
			return r(content.getTarget() + "を襲撃する<よ>。");
		default:
			return Talk.SKIP;
		}
	}

	private Estimate getEstimate() {
		return (Estimate)player.getPretendVillagerEstimate();
	}
}
