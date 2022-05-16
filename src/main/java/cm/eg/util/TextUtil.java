package cm.eg.util;

import cm.eg.model.Article;
import cm.eg.model.WordFreq;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multiset.Entry;
import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.corpus.tag.Nature;
import com.hankcs.hanlp.seg.common.Term;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class TextUtil {

	/**
	 * 从文件中获取数据，封装成对象。会将词频排序，在多个任务的情况下性能得到优化
	 * 
	 * @param file
	 */
	public static Article getArticle(File file) throws IOException, TikaException {
		String text = getString(file);
		List<Term> segmentList = getSegmentList(text);
		List<WordFreq> wordFreqList = getWordFrequency(segmentList);
		// 词频从高到低排序
		wordFreqList.sort((a, b) -> Integer.compare(b.getFreq(), a.getFreq()));

		// 封装Article
		Article article = new Article();
		article.setName(file.getName());
		article.setText(text);
		article.setSegmentList(segmentList);
		article.setWordFreqList(wordFreqList);
		return article;
	}

	/**
	 * 从指定文件读取一整个字符串
	 * 
	 * @param file
	 */
	private static String getString(File file) throws IOException, TikaException {
		Tika tika = new Tika();
		tika.setMaxStringLength((int) file.length());
		String str = tika.parseToString(file);
		return str;
	}

	/**
	 * 将输入的字符串分词处理。
	 * 
	 * @param text 文本
	 * @return 切分后的单词
	 */
	private static List<Term> getSegmentList(String text) {
		//标准分词，返回一个ArrayList类型
		List<Term> segmentList = HanLP.segment(text);
		// 过滤器
		//removeIf() 方法用于删除所有满足特定条件的数组元素。
		//new Predicate广泛用在支持lambda表达式的API中
		segmentList.removeIf(new Predicate<Term>() {
			/**
			 * 过滤掉：长度为1的分词、标点符号
			 */
			//   test方法主要用于参数符不符合规则
			public boolean test(Term term) {
				boolean flag = false;
				// 长度
				String real = term.word.trim();
				if (real.length() <= 1) {
					flag = true;
				}
				// 类型
				// 词性以w开头的，为各种标点符号
				if (term.nature.startsWith('w')) {
					flag = true;
				}
				// 过滤掉代码
				if (term.nature.equals(Nature.nx)) {// Nature.nx字母专名
					flag = true;
				}
				return flag;
			}
		});
		return segmentList;
	}

	/**
	 * 根据分词集合统计词频
	 * @param segmentList 词频集合
	 */
	public static List<WordFreq> getWordFrequency(List<Term> segmentList) {
		// 统计词频
		//使用HashMultiset统计某个值出现的次数
		//需要先创建一个HashMap
		Multiset<String> wordSet = HashMultiset.create();
		for (Term term : segmentList) {// 放入词汇集合
			wordSet.add(term.word);
		}
		// 从词汇集合取出单词和频次,放入词频集合
		List<WordFreq> wfList = new ArrayList<>();
		for (Entry<String> entry : wordSet.entrySet()) {
			//entrySet(): 类似与Map.entrySet 返回Set<Multiset.Entry>。包含的Entry支持使用getElement()和getCount()
			wfList.add(new WordFreq(entry.getElement(), entry.getCount()));
		}
		return wfList;
	}

	/**
	 * 最长公共子串。
	 */
	public static String getLCString(String string1, String string2) {
		// 参数检查
		if(string1==null || string2 == null){
			return null;
		}
		if(string1.equals("") || string2.equals("")){
			return null;
		}
		// 矩阵的横向长度
		int len1 = string1.length();
		// 矩阵的纵向长度
		int len2 = string2.length();

		// 保存矩阵的上一行
		int[] topLine = new int[len1];
		// 保存矩阵的当前行
		int[] currentLine = new int[len1];
		// 矩阵元素中的最大值
		int maxLen = 0;
		// 矩阵元素最大值出现在第几列
		int pos = 0;
		char ch = ' ';
		for(int i=0; i<len2; i++){
			ch = string2.charAt(i);
			// 遍历str1，填充当前行的数组
			for(int j=0; j<len1; j++){
				if( ch == string1.charAt(j)){
					// 如果当前处理的是矩阵的第一列，单独处理，因为其坐上角的元素不存在
					if(j==0){
						currentLine[j] = 1;
					} else{
						currentLine[j] = topLine[j-1] + 1;
					}
					if(currentLine[j] > maxLen){
						maxLen = currentLine[j];
						pos = j;
					}
				}
			}
			// 将矩阵的当前行元素赋值给topLine数组; 并清空currentLine数组
			for(int k=0; k<len1; k++){
				topLine[k] = currentLine[k];
				currentLine[k] = 0;
			}
			// 或者采用下面的方法
			// topLine = currentLine;
			// currentLine = new int[len1];
		}
		return string1.substring(pos-maxLen+1, pos+1);
	}
}
