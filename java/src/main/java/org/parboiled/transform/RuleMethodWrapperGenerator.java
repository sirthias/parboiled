package org.parboiled.transform;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Creates wrapper methods for all rule methods which have action parameters to
 * initialize these parameters.
 * 
 * @author Ken Wenzel
 * 
 */
public class RuleMethodWrapperGenerator implements Opcodes, Types {
	public void process(ParserClassNode classNode) {
		for (RuleMethod ruleMethod : classNode.getRuleMethods().values()) {
			if (!ruleMethod.getActionParams().isEmpty()) {
				createRuleMethodWrapper(classNode, ruleMethod);
			}
		}
	}

	private Type[] getParameterTypes(RuleMethod method) {
		List<Type> paramTypes = new ArrayList<Type>(Arrays.asList(Type.getArgumentTypes(method.desc)));

		int i = (method.access & ACC_STATIC) != 0 ? 0 : 1;
		for (Iterator<Type> it = paramTypes.iterator(); it.hasNext();) {
			it.next();

			if (method.getActionParams().get(i)) {
				it.remove();
			}
			i++;
		}

		return paramTypes.toArray(new Type[paramTypes.size()]);
	}

	@SuppressWarnings("unchecked")
	protected void createRuleMethodWrapper(ParserClassNode classNode, RuleMethod method) {
		Type[] newParamTypes = getParameterTypes(method);
		String newName = AsmUtils.renameRule(method.name, method.desc);
		String newDesc = Type.getMethodDescriptor(RULE, newParamTypes);

		MethodNode wrapperMethod = new MethodNode(method.access, method.name, method.desc, method.signature, new String[0]);
		wrapperMethod.visibleAnnotations = method.visibleAnnotations;
		wrapperMethod.visibleParameterAnnotations = method.visibleParameterAnnotations;

		GeneratorAdapter gen = new GeneratorAdapter(wrapperMethod, method.access, method.name, method.desc);
		gen.visitCode();

		// create matcher that initializes the action arguments
		gen.newInstance(INIT_ARGS_MATCHER);
		gen.dup();

		// create original rule
		int offset = 0;
		if ((method.access & ACC_STATIC) == 0) {
			gen.loadThis();
			offset = 1;
		}
		
		Type[] argumentTypes = Type.getArgumentTypes(method.desc);
		
		// load non-action parameters
		for (int param = 0; param < argumentTypes.length; param++) {
			if (! method.getActionParams().get(param + offset)) {
				gen.loadArg(param);
			}
		}
		gen.visitMethodInsn(INVOKEVIRTUAL, classNode.name, newName, newDesc);

		// load action parameters into array
		gen.push(method.getActionParams().cardinality());
		gen.newArray(OBJECT);
		int index = 0;
        for (int param = 0; param < argumentTypes.length; param++) {
            if (method.getActionParams().get(param + offset)) {
                gen.dup();
                gen.push(index);
                gen.loadArg(param);
                gen.box(argumentTypes[param]);
                gen.arrayStore(OBJECT);
            }
        }

		gen.invokeConstructor(INIT_ARGS_MATCHER, new Method("<init>", Type.VOID_TYPE, new Type[] { RULE, Type.getType(Object[].class) }));
		gen.returnValue();
		gen.visitEnd();

		method.name = newName;
		method.desc = newDesc;
		method.visibleParameterAnnotations = null;

		classNode.methods.add(wrapperMethod);
	}
}
