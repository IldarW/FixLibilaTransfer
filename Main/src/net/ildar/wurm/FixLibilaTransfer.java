package net.ildar.wurm;

import com.wurmonline.math.Vector3f;
import com.wurmonline.server.Features;
import com.wurmonline.server.combat.ServerProjectile;
import com.wurmonline.server.creatures.Creature;
import javassist.*;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.Bytecode;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.MethodInfo;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import org.gotti.wurmunlimited.modloader.classhooks.CodeReplacer;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;
import org.gotti.wurmunlimited.modloader.interfaces.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class FixLibilaTransfer implements WurmServerMod, PreInitable, Configurable {
    private static final String VERSION = "1.0";
    private static final Logger logger = Logger.getLogger(FixLibilaTransfer.class.getSimpleName());

    @Override
    public void configure(Properties properties) {
    }

    @Override
    public void preInit() {
        ClassPool classPool = HookManager.getInstance().getClassPool();
        try {
            CtMethod savePlayerToDiskMethod = classPool.getCtClass("com.wurmonline.server.intra.IntraServerConnection")
                    .getMethod("savePlayerToDisk", "([BIIZZ)J");
            MethodInfo methodInfo = savePlayerToDiskMethod.getMethodInfo();
            CodeAttribute codeAttribute = methodInfo.getCodeAttribute();

            //removing assignments 
            //   faith = 0.0f;
            //   favor = 0.0f;
            //   align = 0.0f;
            //   deity = 0;
            //   isPriest = false;
            //to make Libila faith transferable from PVP to PVE servers
            replaceZeroFStore(methodInfo, codeAttribute, 21);
            replaceZeroFStore(methodInfo, codeAttribute, 22);
            replaceZeroFStore(methodInfo, codeAttribute, 23);
            replaceZeroIStore(methodInfo, codeAttribute, 20);
            replaceZeroIStore(methodInfo, codeAttribute, 137);
            methodInfo.rebuildStackMap(classPool);
            logger.info("Fix applied");
        } catch (NotFoundException | BadBytecode e) {
            logger.severe("Failed to modify the method body!");
            e.printStackTrace();
        }
    }

    private void replaceZeroFStore(MethodInfo methodInfo, CodeAttribute codeAttribute, int localVarId) throws NotFoundException, BadBytecode {
        Bytecode bytecode = new Bytecode(methodInfo.getConstPool());
        bytecode.addFconst(0);
        bytecode.addFstore(localVarId);
        byte[] search = bytecode.get();
        bytecode = new Bytecode(methodInfo.getConstPool());
        bytecode.addGap(2);
        byte[] replacement = bytecode.get();
        new CodeReplacer(codeAttribute).replaceCode(search, replacement);
    }

    private void replaceZeroIStore(MethodInfo methodInfo, CodeAttribute codeAttribute, int localVarId) throws NotFoundException, BadBytecode {
        Bytecode bytecode = new Bytecode(methodInfo.getConstPool());
        bytecode.addIconst(0);
        bytecode.addIstore(localVarId);
        byte[] search = bytecode.get();
        bytecode = new Bytecode(methodInfo.getConstPool());
        bytecode.addGap(2);
        byte[] replacement = bytecode.get();
        new CodeReplacer(codeAttribute).replaceCode(search, replacement);
    }

    @Override
    public String getVersion() {
        return VERSION;
    }
}