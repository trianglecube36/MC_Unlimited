package io.github.trianglecube36.unlimited.chunk;

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
		int x;
		int y;
		int i = 512;
		
		for(x = 0;x < stacks.length;x++){
			i += stacks[x].length;
		}
		
		byte[] savea = new byte[i];
		
		for(x = 0;x < 512;x++){
			savea[x] = (byte) ((stacks[(x << 1)].length << 4) | (stacks[(x << 1) + 1].length));
		}
		
		i = 512;
		int[] s;
		for(x = 0;x < 32;x++){
			s = stacks[x];
			for(y = 0;y < s.length){
				
			}
		}
	}
	
	public byte[] save(){
		return null;
	}
}
