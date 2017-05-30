package com.application.threads;// import java.lang.reflect.Method;
//
// import com.itool.Logger;
// import com.itool.LoggerFactory;
// import com.itool.exception.LoggerException;
//
// public class TestClass {
//     static TestClass obj = new TestClass();
//     static Logger logger = LoggerFactory.getLogger();
//
//     public static void main(String[] args) throws LoggerException {
//         System.out.println(System.getProperty("user.home"));
//         class Local {};
//         Method m = Local.class.getEnclosingMethod();
//         System.out.println(m);
//         logger.logEnter(m, args);
//         obj.hello();
//         logger.logExit(m);
//         logger.close();
//     }
//     void hello() throws LoggerException{
//         class Local {};
//         Method m = Local.class.getEnclosingMethod();
//         logger.logEnter(m, new String[]{});
//         System.out.println("hello");
//         logger.logExit(m);
//     }
//
//     @Before("execution (* com.example.CustomWeaving.*(..))")
//     public void adviceEnter(JoinPoint joinPoint) {
//         System.out.printf("Calling method:   '%s'%n",
//                 joinPoint.getSignature().getName() +
//                         "in class " + joinPoint.getSignature().getDeclaringType());
//     }
//
//
// }
