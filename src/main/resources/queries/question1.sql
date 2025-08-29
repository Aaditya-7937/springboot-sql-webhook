/* 
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/SQLTemplate.sql to edit this template
 */
/**
 * Author:  aadit
 * Created: 29-Aug-2025
 */
SELECT 
    P.AMOUNT AS SALARY,
    CONCAT(E.FIRST_NAME, ' ', E.LAST_NAME) AS NAME,
    TIMESTAMPDIFF(YEAR, E.DOB, CURDATE()) AS AGE,
    D.DEPARTMENT_NAME
FROM PAYMENTS P
JOIN EMPLOYEE E ON P.EMP_ID = E.EMP_ID
JOIN DEPARTMENT D ON E.DEPARTMENT = D.DEPARTMENT_ID
WHERE DAY(P.PAYMENT_TIME) <> 1
  AND P.AMOUNT = (
        SELECT MAX(P2.AMOUNT)
        FROM PAYMENTS P2
        WHERE DAY(P2.PAYMENT_TIME) <> 1
    );

