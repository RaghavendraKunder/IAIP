<%@ page language="java" 
         contentType="text/html; charset=UTF-8" 
         pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <title>ExamFlow</title>
    <style>
        body {
            font-family: 'Segoe UI', sans-serif;
            background: #f8fafc;
            display: flex;
            align-items: center;
            justify-content: center;
            min-height: 100vh;
            margin: 0;
        }
        .card {
            background: white;
            border-radius: 16px;
            padding: 3rem;
            text-align: center;
            box-shadow: 0 4px 24px rgba(0,0,0,0.08);
            max-width: 420px;
            width: 90%;
        }
        h1 { color: #6366f1; font-size: 2rem; margin-bottom: 0.5rem; }
        p  { color: #64748b; margin-bottom: 2rem; font-size: 0.95rem; }
        .badge {
            background: #dcfce7;
            color: #16a34a;
            padding: 0.4rem 1rem;
            border-radius: 99px;
            font-size: 0.8rem;
            font-weight: 600;
            margin-bottom: 1.5rem;
            display: inline-block;
        }
        .btn {
            display: inline-block;
            background: linear-gradient(135deg, #6366f1, #8b5cf6);
            color: white;
            padding: 0.75rem 2rem;
            border-radius: 10px;
            text-decoration: none;
            font-weight: 600;
            margin: 0.4rem;
            font-size: 0.9rem;
        }
        .btn-outline {
            display: inline-block;
            border: 2px solid #6366f1;
            color: #6366f1;
            padding: 0.75rem 2rem;
            border-radius: 10px;
            text-decoration: none;
            font-weight: 600;
            margin: 0.4rem;
            font-size: 0.9rem;
        }
    </style>
</head>
<body>
    <div class="card">
        <div class="badge">✅ Server Running</div>
        <h1>🎓 ExamFlow</h1>
        <p>Online Examination System is deployed successfully on Tomcat!</p>
        <a href="api/register" class="btn">Test Register API</a>
        <a href="api/login" class="btn-outline">Test Login API</a>
    </div>
</body>
</html>
