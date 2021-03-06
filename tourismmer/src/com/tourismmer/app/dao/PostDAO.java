package com.tourismmer.app.dao;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.exception.ConstraintViolationException;

import com.tourismmer.app.constants.Messages;
import com.tourismmer.app.model.LikePost;
import com.tourismmer.app.model.ListPost;
import com.tourismmer.app.model.Post;
import com.tourismmer.app.model.UserGo;
import com.tourismmer.app.util.HibernateUtil;
import com.tourismmer.app.util.Util;

public class PostDAO {

	public Post create(Post postParam) {
		
		try {
			
			Session session = HibernateUtil.getSessionFactory().openSession();
			session.beginTransaction();
			
			postParam.setDate(Util.getInstanceCalendar());
			session.save(postParam);
		
			postParam.setStatusCode(Messages.SUCCESS.getStatusCode());
			postParam.setStatusText(Messages.SUCCESS.getStatusText());
			
			session.getTransaction().commit();
			session.close();
		
		} catch (Exception e) {
			postParam.setStatusCode(Messages.ERROR_QUERYING_DATABASE.getStatusCode());
			postParam.setStatusText(Messages.ERROR_QUERYING_DATABASE.getStatusText());
			Log log = LogFactory.getLog(PostDAO.class);
			log.error(e);
		}
		
		return postParam;
		
	}
	
	public UserGo join(UserGo userGoParam) {
		
		try {
			
			Session session = HibernateUtil.getSessionFactory().openSession();
			session.beginTransaction();
			
			Query query = session.createQuery("from UserGo o where o.idUser = :idUser and o.idPost = :idPost");
			query.setParameter("idUser", userGoParam.getIdUser());
			query.setParameter("idPost", userGoParam.getIdPost());
			
			// Se existe registro
			if(query.list().size() >= 1) {
				
				Query queryDel = session.createQuery("DELETE from UserGo o where o.idUser = :idUser and o.idPost = :idPost");
				queryDel.setParameter("idUser", userGoParam.getIdUser());
				queryDel.setParameter("idPost", userGoParam.getIdPost());
				
				queryDel.executeUpdate();
				userGoParam.setStatusCode(Messages.SUCCESS_UNDO.getStatusCode());
				userGoParam.setStatusText(Messages.SUCCESS_UNDO.getStatusText());
				
			} else {
				session.save(userGoParam);
				userGoParam.setStatusCode(Messages.SUCCESS.getStatusCode());
				userGoParam.setStatusText(Messages.SUCCESS.getStatusText());
			}
		
			
			session.getTransaction().commit();
			session.close();
		
		} catch (Exception e) {
			userGoParam.setStatusCode(Messages.ERROR_QUERYING_DATABASE.getStatusCode());
			userGoParam.setStatusText(Messages.ERROR_QUERYING_DATABASE.getStatusText());
			Log log = LogFactory.getLog(PostDAO.class);
			log.error(e);
		}
		
		return userGoParam;
		
	}
	
	public Post getPost(Post postParam) {
		
		try {
			
			Session session = HibernateUtil.getSessionFactory().openSession();
			session.beginTransaction();
			
			Post post = (Post) session.get(Post.class, postParam.getId());
			
			session.getTransaction().commit();
			session.close();
			
			if(post != null) {
				
				postParam.setId(post.getId());
				postParam.setDescription(post.getDescription());
				postParam.setAuthor(post.getAuthor());
				postParam.setImage(post.getImage());
				postParam.setTypePost(post.getTypePost());
				postParam.setDate(post.getDate());
				
				int countComment = session.createQuery("from Comment c where c.post.id = :idPost")
						.setParameter("idPost", postParam.getId()).list().size();
				postParam.setCountComment(countComment);
				
				int countUserGo = session.createQuery("from UserGo u where u.idPost = :idPost")
						.setParameter("idPost", postParam.getId()).list().size();
				postParam.setCountUserGo(countUserGo);
				
				postParam.setStatusCode(Messages.SUCCESS.getStatusCode());
				postParam.setStatusText(Messages.SUCCESS.getStatusText());
					
			} else {
				postParam.setStatusCode(Messages.QUERY_NOT_FOUND.getStatusCode());
				postParam.setStatusText(Messages.QUERY_NOT_FOUND.getStatusText());
			}
			
		
		} catch (Exception e) {
			
			if(e.getCause() instanceof ConstraintViolationException) {
				String msg = ((ConstraintViolationException) e.getCause()).getSQLException().getMessage();
				postParam.setStatusCode(Messages.CONSTRAINT_VIOLATION_EXCEPTION.getStatusCode());
				postParam.setStatusText(msg);
			
			} else {
				postParam.setStatusCode(Messages.ERROR_QUERYING_DATABASE.getStatusCode());
				postParam.setStatusText(e.getMessage());
			}
			
			Log log = LogFactory.getLog(PostDAO.class);
			log.error(e);
		}
		
		return postParam;
		
	}
	
	public ListPost getListPost(Long idGroup, Integer amount, Integer firstResult, Long idUser) {
		
		ListPost listPost = new ListPost();
		
		try {
			
			Session session = HibernateUtil.getSessionFactory().openSession();
			session.beginTransaction();
			
			Query query = session.createQuery("from Post p where p.group.id = :idGroup order by p.date desc");
			query.setParameter("idGroup", Util.getLong(idGroup));
			query.setMaxResults(amount);
			query.setFirstResult(firstResult);
			
			@SuppressWarnings("unchecked")
			List<Post> list = query.list();
			
			if(Util.isEmptyOrNull(list)) {
				listPost.setStatusCode(Messages.QUERY_NOT_FOUND.getStatusCode());
				listPost.setStatusText(Messages.QUERY_NOT_FOUND.getStatusText());
				return listPost;
			}
			
			Post post = null;
			
			for (Post p : list) {
				post = new Post();
				post.setId(p.getId());
				post.setDescription(p.getDescription());
				post.setAuthor(p.getAuthor());
				post.setImage(p.getImage());
				post.setTypePost(p.getTypePost());
				post.setDate(p.getDate());
				
				int countComment = session.createQuery("from Comment o where o.post.id = :idPost")
						.setParameter("idPost", post.getId()).list().size();
				post.setCountComment(countComment);
				
				int countUserGo = session.createQuery("from UserGo o where o.idPost = :idPost")
						.setParameter("idPost", post.getId()).list().size();
				post.setCountUserGo(countUserGo);
				
				Query queryItem = session.createQuery("from LikePost o where o.idUser = :idUser and o.idPost = :idPost");
				queryItem.setParameter("idUser", idUser);
				queryItem.setParameter("idPost", post.getId());
				
				post.setUserLiked(queryItem.list().size() >= 1);
				
				Query queryItem2 = session.createQuery("from Comment o where o.author.id = :idUser and o.post.id = :idPost");
				queryItem2.setParameter("idUser", idUser);
				queryItem2.setParameter("idPost", post.getId());
				
				post.setUserCommented(queryItem2.list().size() >= 1);
				
				Query queryItem3 = session.createQuery("from UserGo o where o.idUser = :idUser and o.idPost = :idPost");
				queryItem3.setParameter("idUser", idUser);
				queryItem3.setParameter("idPost", post.getId());
				
				post.setUserGo(queryItem3.list().size() >= 1);
				
				listPost.getListPost().add(post);
			}
			
			listPost.setStatusCode(Messages.SUCCESS.getStatusCode());
			listPost.setStatusText(Messages.SUCCESS.getStatusText());
			
			session.getTransaction().commit();
			session.close();
		
		} catch (Exception e) {
			
			if(e.getCause() instanceof ConstraintViolationException) {
				String msg = ((ConstraintViolationException) e.getCause()).getSQLException().getMessage();
				listPost.setStatusCode(Messages.CONSTRAINT_VIOLATION_EXCEPTION.getStatusCode());
				listPost.setStatusText(msg);
			
			} else {
				listPost.setStatusCode(Messages.ERROR_QUERYING_DATABASE.getStatusCode());
				listPost.setStatusText(e.getMessage());
			}
			
			Log log = LogFactory.getLog(PostDAO.class);
			log.error(e);
		}
		
		return listPost;
	}
	
	public LikePost like(LikePost likeParam) {
		
		try {
			
			Session session = HibernateUtil.getSessionFactory().openSession();
			session.beginTransaction();
			
			Query query = session.createQuery("from LikePost o where o.idUser = :idUser and o.idPost = :idPost");
			query.setParameter("idUser", likeParam.getIdUser());
			query.setParameter("idPost", likeParam.getIdPost());
			
			// Se existe registro
			if(query.list().size() >= 1) {
				
				Query queryDel = session.createQuery("DELETE from LikePost o where o.idUser = :idUser and o.idPost = :idPost");
				queryDel.setParameter("idUser", likeParam.getIdUser());
				queryDel.setParameter("idPost", likeParam.getIdPost());
				
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

}
