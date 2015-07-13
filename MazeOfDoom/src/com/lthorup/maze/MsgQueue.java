package com.lthorup.maze;

public class MsgQueue {

	Object[] data;
	int size;
	int head, tail;
	
	public MsgQueue(int size) {
		this.size = size+1;
		clear();
	}
	
	public void clear() {
		data = new Object[this.size];
		head = tail = 0;		
	}
	
	public boolean empty() { return head == tail; }
	
	public boolean enqueue(Object o) {
		int nextTail = next(tail);
		if (nextTail == head)
			return false;
		data[tail] = o;
		tail = nextTail;
		return true;
	}
	
	public Object dequeue() {
		if (head == tail)
			return null;
		Object o = data[head];
		data[head] = null;
		head = next(head);
		return o;
	}
	
	private int next(int n) {
		n++;
		if (n == size)
			n = 0;
		return n;
	}
}
