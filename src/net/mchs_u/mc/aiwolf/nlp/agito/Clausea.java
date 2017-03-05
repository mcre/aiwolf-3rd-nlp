package net.mchs_u.mc.aiwolf.nlp.agito;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mychaelstyle.nlp.KNP;

public class Clausea {
	private int id = -1;
	private String text = null;
	private String main = null;
	private Set<String> signages = null;
	private String target = null;
	private Clausea child = null;
	private Set<Clausea> parents = null;
	private Clausea paraForward = null;
	private Clausea paraBack = null;
	
	private Set<String> modalities = null;
	private String kaku = null;
	private Map<String, Clausea> kakuMap = null;
	
	private Set<String> attributes = null;
	
	private String aiwolfWordType = null;
	private String aiwolfWordMeaning = null;
	private boolean negative = false;
	
	private Clausea(int id, String text, String main, String target) {
		this.id = id;
		this.text = text;
		this.main = main;
		this.target = target;
		
		signages = new HashSet<>();
		attributes = new HashSet<>();
		parents = new HashSet<>();
		kakuMap = new HashMap<>();
		modalities = new HashSet<>();
	}
	
	private void addSignage(String signage) {
		signages.add(signage);
	}
	
	private void addAttribute(String attribute) {
		if(attribute.equals(""))
			return;
		
		String att = attribute.replace("\"", "");
		attributes.add(att);
		
		if(att.startsWith("人狼知能,")){
			String[] split = att.split(",");
			aiwolfWordType = split[1];
			aiwolfWordMeaning = split[2];
		}
		
		if(att.equals("否定表現"))
			negative = true;
		
		if(att.startsWith("{解析格:"))
			kaku = att.replace("{解析格:", "").replace("}", "");
		
		if(att.startsWith("モダリティ-"))
			modalities.add(att.replace("モダリティ-", ""));
	}
	
	private void createKakuMap(List<Clausea> clauseas) {
		for(String att: attributes) {
			if(att.startsWith("{格関係")) {
				//TODO 本来は親だけをたどるべきなのだけど実装が面倒そうなので・・・
				String[] sp = att.replace("}", "").split(":");
				Clausea c = null;
				for(Clausea clausea: clauseas) {
					if(clausea.kaku != null && clausea.kaku.equals(sp[1]) && clausea.signages.contains(sp[2])){
						c = clausea;
						break;
					}
				}
				kakuMap.put(sp[1], c);
			}
		}
	}
	
	public static List<Clausea> createClauseas(String text) throws IOException, InterruptedException {
		List<Clausea> clauseas = new ArrayList<>();
		
		KNP knp = new KNP();
		ObjectNode root = knp.parse(text);
		
		for(JsonNode clauseaNode: root.get("clauseas")){
			Clausea clausea = new Clausea(
					clauseas.size(),
					clauseaNode.get("clausea").asText(),
					clauseaNode.get("attributes").findValues("正規化代表表記").get(0).get(0).asText(),
					clauseaNode.get("target").asText());
			
			for(JsonNode clAtt: clauseaNode.get("attributes"))
				clausea.addAttribute(clAtt.toString());
			
			for(JsonNode phraseNode: clauseaNode.get("phrases")) {
				for(JsonNode clPhr: phraseNode.get("attributes"))
					clausea.addAttribute(clPhr.toString());
				
				for(JsonNode morphemeNode: phraseNode.get("morphemes")) {
					clausea.addSignage(morphemeNode.get("signage").asText());
					for(JsonNode clMor: morphemeNode.get("attributes"))
						clausea.addAttribute(clMor.toString());
				}
			}
			
			clauseas.add(clausea);
		}
		
		for(Clausea clausea: clauseas) { // target Pの処理
			if(!clausea.getTarget().endsWith("P"))
				continue;
			
			int id = Integer.parseInt(clausea.getTarget().replace("P", ""));
			clausea.paraForward = clauseas.get(id);
			clauseas.get(id).paraBack = clausea;
		}
		
		for(Clausea clausea: clauseas) { // target Dの処理
			if(!clausea.getTarget().endsWith("D"))
				continue;
			
			int id = Integer.parseInt(clausea.getTarget().replace("D", ""));
			if(id > -1) {
				clausea.child = clauseas.get(id);
				clauseas.get(id).parents.add(clausea);
			}
		}
		
		for(Clausea clausea: clauseas) {
			clausea.createKakuMap(clauseas);
		}
		
		return clauseas;
	}

	
	public String getTarget() {
		return target;
	}
	
	public int getId() {
		return id;
	}

	public String getText() {
		return text;
	}

	public String getMain() {
		return main;
	}

	public Set<String> getSignages() {
		return signages;
	}

	public Clausea getChild() {
		return child;
	}

	public Set<Clausea> getParents() {
		return parents;
	}

	public Clausea getParaForward() {
		return paraForward;
	}

	public Clausea getParaBack() {
		return paraBack;
	}

	public String getKaku() {
		return kaku;
	}

	public Map<String, Clausea> getKakuMap() {
		return kakuMap;
	}

	public Set<String> getAttributes() {
		return attributes;
	}

	public String getAiwolfWordType() {
		return aiwolfWordType;
	}

	public String getAiwolfWordMeaning() {
		return aiwolfWordMeaning;
	}
	
	public Set<String> getModalities() {
		return modalities;
	}

	public boolean isNegative() {
		return negative;
	}
	
	public static Clausea findMainClausea(List<Clausea> clauseas, String main) {
		for(Clausea clausea: clauseas)
			if(clausea.main.equals(main))
				return clausea;
		return null;
	}
	
	public static Clausea findModalityClausea(List<Clausea> clauseas, String modality) {
		for(Clausea clausea: clauseas)
			if(clausea.modalities.contains(modality))
				return clausea;
		return null;
	}	
	
	public static Clausea findAttributeClausea(List<Clausea> clauseas, String attribute) {
		for(Clausea clausea: clauseas)
			if(clausea.attributes.contains(attribute))
				return clausea;
		return null;
	}
	
	public static Clausea findAiwolfTypeClausea(List<Clausea> clauseas, String type) {
		for(Clausea clausea: clauseas)
			if(clausea.aiwolfWordType != null && clausea.aiwolfWordType.equals(type))
				return clausea;
		return null;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("");
		sb.append("* " + text);
		if(child != null)
			sb.append(" ( -> " + child.text + " )");
		for(Clausea parent: parents)
			sb.append(" ( <- " + parent.text + " )");
		
		if(paraForward != null)
			sb.append(" ( ||> " + paraForward.text + " )");
		if(paraBack != null)
			sb.append(" ( <|| " + paraBack.text + " )");		
		//sb.append(" " + target + " ");
		sb.append("\n");
		
		for(String key: kakuMap.keySet()) {
			if(kakuMap.get(key) == null)
				sb.append("<" + null + " " + key + ">\n");
			else
				sb.append("<" + kakuMap.get(key).main + " " + key + ">\n");
		}
		
		sb.append("・" + main + "\n");
		if(kaku != null)
			sb.append("・" + kaku + "格\n");
		if(aiwolfWordType != null)
			sb.append("☆" + aiwolfWordType + " = " + aiwolfWordMeaning + "\n");
		if(negative)
			sb.append("・否定\n");
		for(String m: modalities)
			sb.append("モダリティ = " + m + "\n");
		
		sb.append("　　" + attributes + "\n");
		
		return sb.toString();
	}	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Clausea other = (Clausea) obj;
		if (id != other.id)
			return false;
		return true;
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		String talk = null;
		//talk = "私は人狼ではありません";
		//talk = "太郎と二郎と三郎と四郎は楽しくて嬉しかった";
		//talk = "そうです、彼じゃなく私が人狼なんだよ";
		//talk = "昨日彼を占ったら人狼でした";
		//talk = "人狼なんて居るのかなあ";
		//talk = "私は人狼のことを知らない";
		//talk = "いみがわからないんだけど";
		//talk = "意味がわからないんだけど";
		//talk = "わたしは人狼のことを知らない";
		//talk = "今日はＡｇｅｎｔ［０１］を吊ってね";
		talk = "今日はＡｇｅｎｔ［０１］さんに投票してください";
		//talk = "Ａｇｅｎｔ［０１］さんは人狼だったよ";
		//talk = "人狼ＣＯします";
		//talk = "人狼ＣＯするよ";
		//talk = "Ａｇｅｎｔ［０１］さんの占い結果は人狼だったよ";
		//talk = "Ａｇｅｎｔ［０１］さんを占ったら人狼だったよ";
		//talk = "Ａｇｅｎｔ［０１］さんを占ったら、結果は人狼だったよ";
		//talk = "Ａｇｅｎｔ［０１］さんの占いの結果は人狼だったよ";
		//talk = "Ａｇｅｎｔ［９９］さんが人狼なんでしょ？";
		//talk = "Ａｇｅｎｔ［９９］さん、いっしょに遊ぼうよ！";
		//talk = "君は誰が人狼だと思う？";
		//talk = "今日はＡｇｅｎｔ［０１］に投票しましょうよ";
		//talk = "彼は人狼らしいよ";
		//talk = "彼は人狼かもしれない";
		//talk = "彼は人狼だろう";
		//talk = "彼は人狼だというわけだ。";
		//talk = "君が人狼なんでしょう？";
		//talk = "人狼は誰だと思う？";
		//talk = "君が犯人なんだろう？";
		
		List<Clausea> list = Clausea.createClauseas(talk);
		for(Clausea c: list) {
			System.out.println(c);
		}
		
	}
	
	

}
