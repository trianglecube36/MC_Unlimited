package io.github.trianglecube36.unlimited.chunk;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

import net.minecraft.nbt.NBTTagCompound;

public class StackArray {
	public int[][] stacks; // [x << 6 | z][depth]
	
	public StackArray(){
		stacks = new int[1024][];
	}
	
	public void push(int x, int z, int value){
		int[] stack = stacks[x << 5 | z];
		if(stack.length < 16){
			stack = Arrays.copyOf(stack, stack.length + 1);
			stack[stack.length - 1] = value;
		}else{
			for(int i = 1;i < 16;i++){	
				stack[i - 1] = stack[i];
			}
			stack[stack.length - 1] = value;
		}
	}
	
	public int pop(int x, int z){
		int[] stack = stacks[x << 5 | z];
		int val = stack[stack.length - 1];
		System.arraycopy(stack, 0, stack, 0, stack.length - 1);
		return val;
	}
	
	public int get(int x, int z){
		int[] stack = stacks[x << 5 | z];
		if(stack.length == 0)
			return -30000000;
		return stack[stack.length - 1];
	}
	
	public void set(int x, int z, int value){
		int[] stack = stacks[x << 5 | z];
		stack[stack.length - 1] = value;
	}
	
	public int getAt(int x, int z, int i){
		return stacks[x << 5 | z][i];
	}
	
	public void setAt(int x, int z, int i, int value){
		int[] stack = stacks[x << 5 | z];
		stack[i] = value;
	}
	
	/**
	 * search and remove
	 */
	public boolean sar(int x, int z, int value){
		int[] stack = stacks[x << 5 | z];
		for(int i = 0;i < stack.length;i++){
			if(stack[i] == value){
				i++;
				while(i < stack.length){
					stack[i - 1] = stack[i];
					i++;
				}
				System.arraycopy(stack, 0, stack, 0, stack.length - 1);
				return true;
			}
		}
		return false;
	}
	
	public void loadData(byte[] ba){
		try {
		DataInputStream in = new DataInputStream(new ByteArrayInputStream(ba));
			int i;
			int y;
			byte l;
			for(i = 0;i < 512;i++){
				l = in.readByte();
				stacks[i << 1] = new int[l >>> 4];
				stacks[(i << 1) + 1] = new int[l & 0x0F];
			}
			
			int[] s;
			for(i = 0;i < 1024;i++){
				s = stacks[i];
				for(y = 0;y < s.length;y++){
					s[y] = in.readInt();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public byte[] save(){
		int i;
		int y;
		
		try {
			ByteArrayOutputStream bo = new ByteArrayOutputStream(512);
			DataOutputStream o = new DataOutputStream(bo);

			for(i = 0;i < 512;i++){
				o.writeByte((byte) ((stacks[(i << 1)].length << 4) | (stacks[(i << 1) + 1].length)));
			}
			
			int[] s;
			for(i = 0;i < 1024;i++){
				s = stacks[i];
				for(y = 0;y < s.length;y++){
					o.writeInt(s[y]);
				}
			}
			return bo.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null; // should never get here...
	}
}
