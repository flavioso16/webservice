package com.tourismmer.app.dao;

import java.util.Calendar;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.exception.ConstraintViolationException;

import com.tourismmer.app.constants.Messages;
import com.tourismmer.app.model.Comment;
import com.tourismmer.app.model.LikeComment;
import com.tourismmer.app.model.ListComment;
import com.tourismmer.app.util.HibernateUtil;
import com.tourismmer.app.util.Util;

public class CommentDAO {
	
	public Comment create(Comment commentParam) {
		
		try {
			
			Session session = HibernateUtil.getSessionFactory().openSession();
			session.beginTransaction();
			
			commentParam.setDate(Calendar.getInstance());
			session.save(commentParam);
			
			commentParam.setStatusCode(Messages.SUCCESS.getStatusCode());
			commentParam.setStatusText(Messages.SUCCESS.getStatusText());
			
			session.getTransaction().commit();
			session.close();
		
		} catch (Exception e) {
			commentParam.setStatusCode(Messages.ERROR_QUERYING_DATABASE.getStatusCode());
			commentParam.setStatusText(Messages.ERROR_QUERYING_DATABASE.getStatusText());
			Log log = LogFactory.getLog(CommentDAO.class);
			log.error(e);
		}
		
		return commentParam;
		
	}
	
	public LikeComment like(LikeComment likeParam) {
		
		try {
			
			Session session = HibernateUtil.getSessionFactory().openSession();
			session.beginTransaction();
			
			Query query = session.createQuery("from LikeComment o where o.idUser = :idUser and o.idComment = :idComment");
			query.setParameter("idUser", likeParam.getIdUser());
			query.setParameter("idComment", likeParam.getIdComment());
			
			// Se existe registro
			if(query.list().size() >= 1) {
				
				Query queryDel = session.createQuery("DELETE from LikeComment o where o.idUser = :idUser and o.idComment = :idComment");
				queryDel.setParameter("idUser", likeParam.getIdUser());
				queryDel.setParameter("idComment", likeParam.getIdComment());
				
				queryDel.executeUpdate();
				
				likeParam.setStatusCode(Messages.SUCCESS_UNDO.getStatusCode());
				likeParam.setStatusText(Messages.SUCCESS_UNDO.getStatusText());
				
			} else {
				session.save(likeParam);
				likeParam.setStatusCode(Messages.SUCCESS.getStatusCode());
				likeParam.setStatusText(Messages.SUCCESS.getStatusText());
			}
		
			
			session.getTransaction().commit();
			session.close();
		
		} catch (Exception e) {
			likeParam.setStatusCode(Messages.ERROR_QUERYING_DATABASE.getStatusCode());
			likeParam.setStatusText(Messages.ERROR_QUERYING_DATABASE.getStatusText());
			Log log = LogFactory.getLog(PostDAO.class);
			log.error(e);
		}
		
		return likeParam;
		
	}
	
	public Comment getComment(Comment commentParam) {
		
		try {
			
			Session session = HibernateUtil.getSessionFactory().openSession();
			session.beginTransaction();
			
			Comment comment = (Comment) session.get(Comment.class, commentParam.getId());
			
			if(comment != null) {
				
				commentParam.setId(comment.getId());
				commentParam.setDescription(comment.getDescription());
				commentParam.setAuthor(comment.getAuthor());
				
				int countLikes = session.createQuery("from Like l where l.idComment = :idComment")
						.setParameter("idComment", commentParam.getId()).list().size();
				commentParam.setCountLike(countLikes);
				
				commentParam.setStatusCode(Messages.SUCCESS.getStatusCode());
				commentParam.setStatusText(Messages.SUCCESS.getStatusText());
					
			} else {
				commentParam.setStatusCode(Messages.QUERY_NOT_FOUND.getStatusCode());
				commentParam.setStatusText(Messages.QUERY_NOT_FOUND.getStatusText());
			}
			
			session.getTransaction().commit();
			session.close();
		
		} catch (Exception e) {
			
			if(e.getCause() instanceof ConstraintViolationException) {
				String msg = ((ConstraintViolationException) e.getCause()).getSQLException().getMessage();
				commentParam.setStatusCode(Messages.CONSTRAINT_VIOLATION_EXCEPTION.getStatusCode());
				commentParam.setStatusText(msg);
			
			} else {
				commentParam.setStatusCode(Messages.ERROR_QUERYING_DATABASE.getStatusCode());
				commentParam.setStatusText(e.getMessage());
			}
			
			Log log = LogFactory.getLog(CommentDAO.class);
			log.error(e);
		}
		
		return commentParam;
		
	}
	
	public ListComment getListComment(Long idPost, Integer amount, Integer firstResult) {
		
		ListComment listComment = new ListComment();
		
		try {
			
			Session session = HibernateUtil.getSessionFactory().openSession();
			session.beginTransaction();
			
			Query query = session.createQuery("from Comment c where c.post.id = :idPost order by c.date desc");
			query.setParameter("idPost", Util.getLong(idPost));
			query.setMaxResults(amount);
			query.setFirstResult(firstResult);
			
			@SuppressWarnings("unchecked")
			List<Comment> list = query.list();
			
			if(Util.isEmptyOrNull(list)) {
				listComment.setStatusCode(Messages.QUERY_NOT_FOUND.getStatusCode());
				listComment.setStatusText(Messages.QUERY_NOT_FOUND.getStatusText());
				return listComment;
			}
			
			Comment comment = null;
				
			for (Comment c : list) {
				comment = new Comment();
				comment.setId(c.getId());
				comment.setDescription(c.getDescription());
				comment.setAuthor(c.getAuthor());
				comment.setDate(c.getDate());
				
				int countLikes = session.createQuery("from Like l where l.idComment = :idComment")
						.setParameter("idComment", comment.getId()).list().size();
				comment.setCountLike(countLikes);
				
				listComment.getListComment().add(comment);
			}
			
			listComment.setStatusCode(Messages.SUCCESS.getStatusCode());
			listComment.setStatusText(Messages.SUCCESS.getStatusText());
				
			
			session.getTransaction().commit();
			session.close();
		
		} catch (Exception e) {
			
			if(e.getCause() instanceof ConstraintViolationException) {
				String msg = ((ConstraintViolationException) e.getCause()).getSQLException().getMessage();
				listComment.setStatusCode(Messages.CONSTRAINT_VIOLATION_EXCEPTION.getStatusCode());
				listComment.setStatusText(msg);
			
			} else {
				listComment.setStatusCode(Messages.ERROR_QUERYING_DATABASE.getStatusCode());
				listComment.setStatusText(e.getMessage());
			}
			
			Log log = LogFactory.getLog(GroupDAO.class);
			log.error(e);
		}
		
		return listComment;
	}

}
