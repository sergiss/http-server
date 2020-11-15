package com.delmesoft.httpserver.utils;

import java.util.HashMap;
import java.util.Map;

/*
 * Copyright (c) 2020, Sergio S.- sergi.ss4@gmail.com http://sergiosoriano.com
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. Neither the name of the copyright holder nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *    	
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. 
 */
public class PathTree<V> {

	private PathTreeNode<V> root;

	public PathTree() {
		root = new PathTreeNode<>();
	}

	public void add(String path, V value) {
		String[] result = path.split("/");
		root.insert(result, 0, value);
	}

	public V remove(String path) {
		String[] result = path.split("/");
		return root.remove(result, 0);
	}

	public V get(String path) {
		String[] result = path.split("/");
		return root.get(result, 0);
	}
	
	public static class PathTreeNode<V> {

		private String dirPath;
		private V value;

		private Map<String, PathTreeNode<V>> nodeMap = new HashMap<>();
		private PathTreeNode<V> parent;

		public String getDirPath() {
			return dirPath;
		}
		
		public void setDirPath(String dirPath) {
			this.dirPath = dirPath;
		}

		public V getValue() {
			return value;
		}

		public void setValue(V value) {
			this.value = value;
		}
		
		public Map<String, PathTreeNode<V>> getNodeMap() {
			return nodeMap;
		}

		public void setNodeMap(Map<String, PathTreeNode<V>> nodeMap) {
			this.nodeMap = nodeMap;
		}
				
		public PathTreeNode<V> getParent() {
			return parent;
		}

		public void setParent(PathTreeNode<V> parent) {
			this.parent = parent;
		}

		public V get(String[] values, int index) {
			index++;
			if (index < values.length) {
				// Search
				PathTreeNode<V> node = nodeMap.get(values[index]);
				if (node != null) {
					return node.get(values, index);
				}
			}
			return value;
		}
		
		public V remove(String[] values, int index) {
			index++;
			if (index >= values.length) {
				nodeMap.clear();
				return this.value;
			} else {
				this.value = null;
				final String dirPath = values[index];
				// Search
				PathTreeNode<V> node = nodeMap.remove(dirPath);
				if (node != null) {
					return node.remove(values, index);
				}
				return null;
			}
		}

		public V insert(String[] values, int index, V value) {
			index++;
			if (index >= values.length) {
				nodeMap.clear();
				V tmp = this.value;
				this.value = value;
				return tmp;
			} else {
				this.value = null;
				final String dirPath = values[index];
				// Search
				PathTreeNode<V> node = nodeMap.get(dirPath);
				if (node == null) {
					node = new PathTreeNode<>();
					node.setDirPath(dirPath);
					node.setParent(this);
					nodeMap.put(dirPath, node); // Add
				}
				return node.insert(values, index, value);
			}
		}

	}

}
