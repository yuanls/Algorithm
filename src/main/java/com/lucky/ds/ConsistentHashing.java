package com.lucky.ds;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import com.lucky.hash.MurMurHash;

/** 一致性Hash */
public class ConsistentHashing {
	// ------------------ 一致性哈希算法的java实现 ------------------
	private SortedMap<Long, String> ketamaNodes = new TreeMap<Long, String>();
	private int numberOfReplicas = 1024;
	private List<String> nodes;
	private volatile boolean init = false; // 标志是否初始化完成
	// 有参数构造函数

	public ConsistentHashing(int numberOfReplicas, List<String> nodes) {
		this.numberOfReplicas = numberOfReplicas;
		this.nodes = new ArrayList<String>(nodes);
		init();
	}

	// 根据key的哈希值，找到最近的一个节点（服务器）
	public String getNodeByKey(String key) {
		if (!init)
			throw new RuntimeException("init uncomplete...");
		long hash = MurMurHash.hash(key);
		// 如果找到这个节点，直接取节点，返回
		if (!ketamaNodes.containsKey(hash)) {
			// 得到大于当前key的那个子Map，然后从中取出第一个key，就是大于且离它最近的那个key
			SortedMap<Long, String> tailMap = ketamaNodes.tailMap(hash);
			if (tailMap.isEmpty()) {
				hash = ketamaNodes.firstKey();
			} else {
				hash = tailMap.firstKey();
			}

		}
		return ketamaNodes.get(hash);
	}

	// 新增节点
	public synchronized void addNode(String node) {
		init = false;
		nodes.add(node);
		init();
	}

	private void init() {
		// 对所有节点，生成numberOfReplicas个虚拟节点
		for (String node : nodes) {
			for (int i = 0; i < numberOfReplicas; i++) {
				// 为这组虚拟结点得到惟一名称
				Long k = MurMurHash.hash(node + i);
				ketamaNodes.put(k, node);
			}
		}
		init = true;
	}

	public void printNodes() {
		for (Long key : ketamaNodes.keySet()) {
			System.out.println(ketamaNodes.get(key));
		}
	}

	public static void main(String[] args) {
		List<String> nodes = Arrays.asList("node1", "node2");
		ConsistentHashing consistentHashing = new ConsistentHashing(2, nodes);

		System.out.println(consistentHashing.getNodeByKey("s1"));
		System.out.println(consistentHashing.getNodeByKey("s2"));
		consistentHashing.addNode("node3");
		System.out.println(consistentHashing.getNodeByKey("s1"));
		System.out.println(consistentHashing.getNodeByKey("s2"));
		System.out.println(consistentHashing.getNodeByKey("s3"));
	}
}
